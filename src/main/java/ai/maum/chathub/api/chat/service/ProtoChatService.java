package ai.maum.chathub.api.chat.service;

import ai.maum.chathub.api.admin.service.MonitorLogService;
import ai.maum.chathub.api.chat.dto.ApiKeyInfo;
import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.dto.elastic.ElasticConfig;
import ai.maum.chathub.api.chat.dto.openai.OpenAIConfig;
import ai.maum.chathub.api.chat.handler.ChatGrpcConnectionHandler;
import ai.maum.chathub.api.chat.handler.ChatScheduleManager;
import ai.maum.chathub.api.chat.handler.ChatScheduleTask;
import ai.maum.chathub.api.chat.handler.ChatStreamObserverHandler;
import ai.maum.chathub.api.chatbotInfo.entity.ChatbotInfoIdEntity;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.api.chatroom.dto.ChatroomDetail;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.dto.EngineParamsConverter;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.service.MemberDetailService;
import ai.maum.chathub.api.skins.service.CallSkinsService;
import ai.maum.chathub.api.weather.service.WeatherService;
import ai.maum.chathub.mybatis.vo.*;
import ai.maum.chathub.util.JSONUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import ai.maum.chathub.util.StringUtil;
import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rag_service.rag_module.Rag;
import rag_service.rag_module.Rag.ChatRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProtoChatService {
    private final ChatGrpcConnectionHandler connectionHandler;
    private final MonitorLogService monitorLogService;
    private final EngineService engineService;
    private final ChatService chatService;
    private final ChatroomService chatroomService;
    private final CallSkinsService callSkinsService;
    private final ChatScheduleManager chatScheduleManager;
    private final WeatherService weatherService;
    private final MemberDetailService memberDetailService;
    private final ChatbotInfoService chatbotInfoService;

    @Value("${service.grpc.chat.max-execution-count}")
    private int maxExecutionCount;
    @Value("${service.grpc.chat.execution-period}")
    private int executionPeriod;

    @Value("${service.weather.longitude}")
    private Float weatherLongitude;
    @Value("${service.weather.latitude}")
    private Float weatherLatitude;

    private void setEngineParamNullToDefault(OpenAIConfig paramConfig, OpenAIConfig defaultConfig) {
        if (paramConfig.getFrequencyPenalty() == null) {
            paramConfig.setFrequencyPenalty(
                    defaultConfig.getFrequencyPenalty() != null ? defaultConfig.getFrequencyPenalty() : 0
            );
        }
        if (paramConfig.getTemperature() == null) {
            paramConfig.setTemperature(
                    defaultConfig.getTemperature() != null ? defaultConfig.getTemperature() : 1.0
            );
        }
        if (paramConfig.getMaxTokens() == null) {
            paramConfig.setMaxTokens(
                    defaultConfig.getMaxTokens() != null ? defaultConfig.getMaxTokens() : 2048
            );
        }
        if (paramConfig.getTopK() == null) {
            paramConfig.setTopK(
                    defaultConfig.getTopK() != null ? defaultConfig.getTopK() : 5
            );
        }
        if (paramConfig.getPresencePenalty() == null) {
            paramConfig.setPresencePenalty(
                    defaultConfig.getPresencePenalty() != null ? defaultConfig.getPresencePenalty() : 0
            );
        }
        if (paramConfig.getTopP() == null) {
            paramConfig.setTopP(
                    defaultConfig.getTopP() != null ? defaultConfig.getTopP() : 0.9
            );
        }

    }

    private JSONObject extractJSONFromItem(Map<String, Object> item) {
        try {
            PGobject value = (PGobject) item.get("json_value");
            JSONObject jsonObject = new JSONObject();
            return new JSONObject(value.getValue());
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private ApiKeyInfo getApiKey (Map<Long, ApiKeyInfo> apiKeyMap, List<Rag.ChatConfig.LLMEngine> llmEngines, Long engineId) {

        ApiKeyInfo apiKeyInfo = apiKeyMap.get(engineId);

        if(apiKeyInfo == null) {
            EngineVO engine = engineService.getEngineByIdWithMapper(engineId);
            if(engine != null) {
                String provider = "azure";
                String vendor = engine.getVendor()!=null?engine.getVendor().toLowerCase():"openai";
                String modelType = engine.getModel();
                if("maumai".equals(vendor)) {
                    provider = "maum";
                } else if("onpremise".equals(vendor)) {
                    provider = "maum_onpremise";
                }
                else if(engine.getEndpoint() == null || engine.getEndpoint().isBlank()){
                    provider = "openai";
                }

                Rag.APIKey apiKey = Rag.APIKey.newBuilder()
                        .setName(engine.getModel())
                        .setEndpoint(engine.getEndpoint()==null?"":engine.getEndpoint())
                        .setVersion(engine.getVersion()==null?"":engine.getVersion())
                        .setKey(engine.getApik())
                        .build();

                int engineIdx = apiKeyMap.size() + 1;
                apiKeyInfo = new ApiKeyInfo(engineIdx, apiKey);
                apiKeyMap.put(engineId, apiKeyInfo);
                Rag.ChatConfig.LLMEngine llmEngine = Rag.ChatConfig.LLMEngine.newBuilder()
                        .setKey(apiKey)
                        .setModelIdx(engineIdx)
                        .setProvider(provider)
                        .setModelType(modelType)
                        .build();
                llmEngines.add(llmEngine);
            }
        }

        return apiKeyInfo;
    }

    private String getNodeValue(Map<String, Object> chatbotInfo, String key) {
        JSONObject converstaion = extractJSONFromItem(chatbotInfo);
        return getNodeValue(converstaion, key);
    }

    private String getNodeValue(JSONObject converstaion, String key) {
        if(converstaion.has(key))
            return String.valueOf(converstaion.get(key));
        else
            return null;
    }

    public void setLLMNode(Map<String, Object> chatbotInfo, Map<Long, ApiKeyInfo> apiKeyMap,
                            List<Rag.ChatConfig.LLMEngine> llmEngines, Rag.ChatConfig.LLMNode.NodeIndex nodeIndex, List<Rag.ChatConfig.LLMNode> llmNodes, Boolean disableNode) {
        setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, null, disableNode);
    }

    public void setLLMNode(Map<String, Object> chatbotInfo, Map<Long, ApiKeyInfo> apiKeyMap,
                            List<Rag.ChatConfig.LLMEngine> llmEngines, Rag.ChatConfig.LLMNode.NodeIndex nodeIndex, List<Rag.ChatConfig.LLMNode> llmNodes, String userPrompt, Boolean disableNode) {
        JSONObject converstaion = extractJSONFromItem(chatbotInfo);
        setLLMNode(converstaion, apiKeyMap, llmEngines, nodeIndex, llmNodes, userPrompt, disableNode);
    }

    private void setLLMNode(JSONObject converstaion, Map<Long, ApiKeyInfo> apiKeyMap,
                            List<Rag.ChatConfig.LLMEngine> llmEngines, Rag.ChatConfig.LLMNode.NodeIndex nodeIndex, List<Rag.ChatConfig.LLMNode> llmNodes, String userPrompt, Boolean disableNode) {

        Boolean bFnLLM = false;
        if(nodeIndex == Rag.ChatConfig.LLMNode.NodeIndex.FUNCTION_CALL) {
            bFnLLM = true;
            log.debug("SET LLM NODE For FunctionCall LLM!!!");
        }

        Integer llmEngineId = 0;
        if(converstaion.has("llm_engine_id") && !bFnLLM)
            llmEngineId = converstaion.getInt("llm_engine_id");
        else if (converstaion.has("function_llm_engine_id") && bFnLLM) {
            try {
                llmEngineId = converstaion.getInt("function_llm_engine_id");
            } catch (Exception e) {
                llmEngineId =2;
            }
        }
        else if(bFnLLM)
            llmEngineId =2;
        ApiKeyInfo apiKeyInfo = getApiKey(apiKeyMap, llmEngines, Long.valueOf(llmEngineId));

        Integer fallbackEngineId = 0;
        if(converstaion.has("fallback_engine_id") && !bFnLLM)
            fallbackEngineId = converstaion.getInt("fallback_engine_id");
        else if(converstaion.has("function_fallback_engine_id") && bFnLLM && converstaion.get("function_fallback_engine_id") != null) {
            try {
                fallbackEngineId = converstaion.getInt("function_fallback_engine_id");
            } catch (Exception e) {
                fallbackEngineId = 3;
            }
        }
        else if(bFnLLM)
            fallbackEngineId = 3;
        ApiKeyInfo apiKeyInfoFallBack = getApiKey(apiKeyMap, llmEngines, Long.valueOf(fallbackEngineId));

        Integer retry = 0;
        if(converstaion.has("retry") && !bFnLLM)
            retry = converstaion.getInt("retry");
        else if(converstaion.has("function_retry") && bFnLLM && converstaion.get("function_retry") != null) {
            try {
                retry = converstaion.getInt("function_retry");
            } catch (Exception e) {
                retry = 2;
            }
        }
        else if(bFnLLM)
            retry = 2;

        OpenAIConfig llmParam = null;
        if(converstaion.has("parameters")) {
            EngineParamsConverter converter = new EngineParamsConverter();
            List<EngineParam> params = converter.convertToEntityAttribute((String) converstaion.get("parameters").toString());
            llmParam = new OpenAIConfig(params);
        }
        String systemPrompt = null;
        if(converstaion.has("system_prompt"))
            systemPrompt = converstaion.getString("system_prompt");
//        String userPrompt = null;
        if(converstaion.has("user_prompt") && userPrompt == null)
            userPrompt = converstaion.getString("user_prompt");

        Rag.LLMParameter llmParameter = Rag.LLMParameter.newBuilder()
                .setTopP(llmParam.getTopP().floatValue())
                .setTemperature(llmParam.getTemperature().floatValue())
                .setPresencePenalty(llmParam.getPresencePenalty().floatValue())
                .setFrequencyPenalty(llmParam.getFrequencyPenalty().floatValue())
                .setMaxTokens(llmParam.getMaxTokens())
                .setSystemPrompt(systemPrompt)
                .setUserPrompt(userPrompt)
                .build();

//        boolean disableNode = false;
//        if(converstaion.has("disable"))
//            disableNode = converstaion.getBoolean("disable");

        Rag.ChatConfig.LLMNode llmNode = Rag.ChatConfig.LLMNode.newBuilder()
                .setNodeIdx(nodeIndex)
                .setMainModelIdx(apiKeyInfo.getEngindIdx())
                .setRetries(retry)
                .setFallbackModelIdx(apiKeyInfoFallBack.getEngindIdx())
                .setModelParams(llmParameter)
                .setDisableNode(disableNode)
                .build();
        llmNodes.add(llmNode);
    }

    public Flux<String> startChat(Long chatbotId, Long chatroomId, String roomId, List<ChatMessage> messages, String userKey, Boolean extChannel, MemberDetail user) throws Exception {
        return startChat(chatbotId, chatroomId, roomId, messages, userKey, extChannel, user, false);
//        return startChat(chatbotId, chatroomId, roomId, messages, userKey, extChannel, user, true);
    }
    public Flux<String> startChat(Long chatbotId, Long chatroomId, String roomId, List<ChatMessage> messages, String userKey, Boolean extChannel, MemberDetail user, Boolean bWeatherInfo) throws Exception {
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String role = lastMessage.getRole();
        Long seq = lastMessage.getSeq() == null ? chatroomService.getChatroomSeq(chatroomId) : lastMessage.getSeq(); //seq가 null이면 seq generate. 겹칠수도 있으나 일단 go..
        String userMsg = lastMessage.getContent();
//        int multiTurn = chatbot.getMultiTurn()==null||chatbot.getMultiTurn()==0?5:chatbot.getMultiTurn();

        log.debug("[GRPC_CHAT] before insert chatroom detail...");
        //질문내용을 chatroom detail에 넣어 줌.
        ChatroomDetail chatroomDetail = new ChatroomDetail(chatroomId, seq, role, userMsg);
        log.debug("[GRPC_CHAT] after insert chatroom detail...:" + ObjectMapperUtil.writeValueAsString(chatroomDetail));
        ChatroomDetailEntity chatroomDetailEntity = chatroomService.setChatroomDetail(new ChatroomDetailEntity(chatroomId, seq, role, userMsg));
        Long chatroomDetailId = chatroomDetailEntity.getId();
        ChatroomDetailVO chatroomDetailVO = chatroomService.getChatRoomDetailByIdFromMapper(chatroomDetailId);

        monitorLogService.ChatMonitorLog(chatroomId, seq, "start chat" , userMsg);

//        Flux<String> fluxString = Flux.create((FluxSink<String> sink) -> {
        return Flux.create((FluxSink<String> sink) -> {

            try {
                String gRpcRoomId = String.valueOf(chatroomId);
                Boolean isNewChannel = false;

                //챗봇정보 가져오기
                ChatbotInfoIdEntity chatbotInfoIdEntity = null;

                //외부채널(카카오)에서의 채팅일 경우 공용챗봇을 이용하게 되므로 chatbotId 만으로 챗봇 정보를 가져온다.
//                if(extChannel)
//                    chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);
//                else
//                    chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoByIdAndUserId(chatbotId, userKey);

                // 채팅은 다른 사람이 만든 챗봇으로도 가능하게 해야 해서 변경
                chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);

                if (chatbotInfoIdEntity == null) {
                    log.error("챗봇정보 없음:" + chatbotId);
                    sink.next("챗봇정보가 없습니다.");
                    sink.complete();
                    return;
                } else if("P".equals(chatbotInfoIdEntity.getEmbeddingStatus())) {
                    log.error("채팅불가:엠베딩중:" + chatbotId);
                    sink.next("데이터 처리중으로 답변이 어렵습니다. 잠시 후 사용해 주세요.");
                    sink.complete();
                    return;
                }

                ChatbotVO chatbot = new ChatbotVO();
                chatbot.setId(chatbotId);
                chatbot.setName(chatbotInfoIdEntity.getName());
                ChatStreamObserverHandler requestObserver = connectionHandler.getStreamObserver(gRpcRoomId);

                if (requestObserver == null) {
                    requestObserver = connectionHandler.createStreamObserver(gRpcRoomId, chatbot, chatroomDetailVO, userKey);
                    isNewChannel = true;
                    monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "GRPC Channel", "Create GRPC Channel");
                } else {
                    monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "Get GRPC Channel", "Get GRPC Channel");
                }

                if (requestObserver == null) {// GRPC 커넥션 생성 실패시 오류
                    sink.next("오류가 발생했습니다. 잠시후 다시 사용해 주세요. (챗봇정보 없음:" + chatbotId + ")");
                    sink.complete();
                    connectionHandler.connectionReset(chatroomId);
                    return;
                }

//                monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "Getting GRPC Channel");

                /* schdule 등록 */
                ChatScheduleTask chatScheduleTask = new ChatScheduleTask(maxExecutionCount, chatScheduleManager, sink, userKey, chatbotId, chatroomDetail.getRoomId(), userMsg);
                ScheduledFuture<?> scheduledFuture = chatScheduleManager.scheduledFuture(chatScheduleTask, 0, executionPeriod, TimeUnit.SECONDS);
                chatScheduleTask.setScheduledFuture(scheduledFuture);

                requestObserver.setBaseStreamObserver(connectionHandler, sink, chatService, chatroomDetailVO, chatScheduleTask);
//                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


                if (isNewChannel) {

                    String configLogString = "Send Config to Langchain\n";

                    JSONObject userPrompt = new JSONObject();
                    JSONObject llmCommon = new JSONObject();
                    JSONObject elasticSearch = new JSONObject();

                    List<Map<String, Object>> chatbotInfoMap = chatbotInfoService.getChatbotInfoMap(chatbotId);
                    Rag.ChatConfig.MemoryType memoryType = Rag.ChatConfig.MemoryType.WINDOW_MEMORY;
                    int memoryWindow = 5;
                    List<Rag.ChatConfig.PreInfo> preInfoList = new ArrayList<Rag.ChatConfig.PreInfo>();
                    List<Rag.Chat> chatHistory = new ArrayList<Rag.Chat>();
                    List<Rag.ChatConfig.LLMEngine> llmEngines = new ArrayList<Rag.ChatConfig.LLMEngine>();
                    List<Rag.ChatConfig.LLMNode> llmNodes = new ArrayList<Rag.ChatConfig.LLMNode>();
                    List<Rag.ChatConfig.Function> functions = new ArrayList<Rag.ChatConfig.Function>();
                    Map<Long, ApiKeyInfo> apiKeyMap = new HashMap<Long, ApiKeyInfo>();
                    Rag.ChatConfig.ESConfig esConfig = Rag.ChatConfig.ESConfig.newBuilder().build();

                    Long embeddingEngineId = 1L;

                    //변경된 구조(chathub)에서는 user_prompt가 별도로 없고 library로 대체
                    //일단 채팅은 돌아야 하기 때문에 user_prompt를 별도로 박아줌.
                    Map<String, Object> userPromptTmp = new HashMap<String, Object>();
                    try {
                        if(extChannel) //만약 외부 채널(카카오채널)을 통한 채팅이면 member_detail_library 활용
                            userPromptTmp = chatbotInfoService.getLibraryDetailByUserId(Long.valueOf(userKey));
                        else
                            userPromptTmp = chatbotInfoService.getUserPromptByChatbotId(chatbotId);
                    } catch (Exception e) {
                        //db에 userPrompt가 없으면 기본 userPromt로 세팅
                        userPromptTmp = chatbotInfoService.getUserPromptByChatbotId(0L);
                    }

                    if(userPromptTmp != null && userPromptTmp.get("json_value") != null) {
                        try {
                            userPrompt = extractJSONFromItem(userPromptTmp);
                            if (userPrompt.has("member_info")) {
                                String userName = user == null ? "마음AI" : user.getName();
                                String memberInfo = userPrompt.getString("member_info").replaceAll("\\{name\\}", userName);
                                preInfoList.add(
                                        Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.PERSONAL)
                                                .setIdx(0)
                                                .setData(memberInfo)
                                                .build()
                                );
                            }
                            if (userPrompt.has("measure_info")) {
                                preInfoList.add(
                                        Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.SKIN_DIAGNOSIS)
                                                .setIdx(1)
                                                .setData(userPrompt.getString("measure_info"))
                                                .build()
                                );
                            }
                            if (userPrompt.has("gene_info")) {
                                preInfoList.add(
                                        Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.DNA_DIAGNOSIS)
                                                .setIdx(2)
                                                .setData(userPrompt.getString("gene_info"))
                                                .build()
                                );
                            }
                            if (userPrompt.has("consult_info")) {
                                preInfoList.add(
                                        Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.INTERVIEW)
                                                .setIdx(3)
                                                .setData(userPrompt.getString("consult_info"))
                                                .build()
                                );
                            }
                            String prettyUserPrompt = JSONUtil.formatJSON(userPrompt);
                            monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "get user information", prettyUserPrompt);
                        } catch (Exception e) {
                            log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                            sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:UserPrompt)");
                            sink.complete();
                            connectionHandler.connectionReset(chatroomId);
                            chatScheduleTask.stop();
                            return;
                        }
                    }

                    if (chatbotInfoMap != null && chatbotInfoMap.size() > 0) {
                        for (Map<String, Object> chatbotInfo : chatbotInfoMap) {
                            Rag.ChatConfig.LLMNode.NodeIndex nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.NULL;
                            String key = (String) chatbotInfo.get("json_key");
                            switch (key) {
                                /*
                                case ("user_prompt"):
                                    userPrompt = extractJSONFromItem(chatbotInfo);

                                    //임시세팅
//                                    PERSONAL = 0; // 개인정보
//                                    SKIN_DIAGNOSIS = 1; // 피부 진단 정보
//                                    DNA_DIAGNOSIS = 2; // 유전자 진단 정보
//                                    INTERVIEW = 3; // 대면 상담내역
//                                    WEATHER = 4;

                                    if (userPrompt.has("member_info")) {
                                        preInfoList.add(
                                                Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.PERSONAL)
                                                        .setIdx(0)
                                                        .setData(userPrompt.getString("member_info"))
                                                        .build()
                                        );
                                    }
                                    if (userPrompt.has("measure_info")) {
                                        preInfoList.add(
                                                Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.SKIN_DIAGNOSIS)
                                                        .setIdx(1)
                                                        .setData(userPrompt.getString("measure_info"))
                                                        .build()
                                        );
                                    }
                                    if (userPrompt.has("gene_info")) {
                                        preInfoList.add(
                                                Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.DNA_DIAGNOSIS)
                                                        .setIdx(2)
                                                        .setData(userPrompt.getString("gene_info"))
                                                        .build()
                                        );
                                    }
                                    if (userPrompt.has("consult_info")) {
                                        preInfoList.add(
                                                Rag.ChatConfig.PreInfo.newBuilder()
//                                                        .setType(Rag.ChatConfig.PreInfo.Type.INTERVIEW)
                                                        .setIdx(3)
                                                        .setData(userPrompt.getString("consult_info"))
                                                        .build()
                                        );
                                    }
                                    String prettyUserPrompt = JSONUtil.formatJSON(userPrompt);
                                    monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "get user information", prettyUserPrompt);

                                    break;
                                 */
                                case ("llm_common"):
                                    try {
                                        llmCommon = extractJSONFromItem(chatbotInfo);
                                        if (llmCommon.has("memory_type")) {
                                            memoryType = Rag.ChatConfig.MemoryType.forNumber(llmCommon.getInt("memory_type"));
                                        }
                                        if (llmCommon.has("window_size")) {
//                                        memoryWindow = llmCommon.getInt("memory_window_size");
                                            memoryWindow = llmCommon.getInt("window_size");
                                        }
                                        //히스토리 가져오기
                                        Integer multiTurn = memoryWindow;
                                        if (memoryType == Rag.ChatConfig.MemoryType.BUFFER_MEMORY)
                                            multiTurn = null;


                                        if (multiTurn != null && multiTurn > 0) {

                                            List<ChatHistoryVO> chatroomDetailList = chatroomService.getChatHistoryForMultiturn(chatroomId, multiTurn);

                                            for (ChatHistoryVO history : chatroomDetailList) {
                                                if (history != null && history.getInput() != null && history.getOutput() != null) {
                                                    Rag.Chat chat = Rag.Chat.newBuilder()
                                                            .setInput(history.getInput())
                                                            .setOutput(history.getOutput())
                                                            .build();
                                                    chatHistory.add(chat);
                                                }
                                            }
                                            String chatHistoryListString = chatroomDetailList.stream()
                                                    .map(chatHistoryVO -> "{" +
                                                            "\"input\":\"" + chatHistoryVO.getInput() + '\"' +
                                                            ", \"output\":\"" + chatHistoryVO.getOutput() + '\"' +
                                                            '}')
                                                    .collect(Collectors.joining(", ", "[", "]"));

                                            monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "get chatHistory", chatroomDetailList.size() + chatHistoryListString);
                                        } else {
                                            monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "get chatHistory", "window_size:" + multiTurn);
                                        }
                                    } catch (Exception e) {
                                        log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                        sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:llm-common)");
                                        sink.complete();
                                        connectionHandler.connectionReset(chatroomId);
                                        chatScheduleTask.stop();
                                        return;
                                    }
                                    break;
                                case ("normal_conversation"):
                                    try {
                                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.NORMAL_CHAT;
                                        setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, false);
                                    } catch (Exception e) {
                                         log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                        sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:normal_conversation)");
                                        sink.complete();
                                        connectionHandler.connectionReset(chatroomId);
                                        chatScheduleTask.stop();
                                        return;
                                    }
                                    break;
                                case ("reproduce_question"):
                                    try {
                                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.QUERY_REWRITE;
                                        setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, !StringUtil.parseBoolean(getNodeValue(chatbotInfo, "use_yn")));
                                    } catch (Exception e) {
                                        log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                        sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:reproduce_question)");
                                        sink.complete();
                                        connectionHandler.connectionReset(chatroomId);
                                        chatScheduleTask.stop();
                                        return;
                                    }
                                    break;
                                case ("rag"):
                                    //사용여부 체크
                                    try {
                                        Boolean disableNode = false;
                                        String useYn = getNodeValue(chatbotInfo, "use_yn");
                                        List<Long> fList = StringUtil.convertStringToLongList(getNodeValue(chatbotInfo, "functions"));

                                        if (useYn == null || useYn.isBlank() || !StringUtil.parseBoolean(useYn)
                                                || fList == null || fList.size() < 1) {
                                            disableNode = true;
                                        }

                                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.RAG_CHAT;
                                        setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, disableNode);

                                        //rag노드 하위의 functioncall 처리
                                        PGobject pgRag = (PGobject) chatbotInfo.get("json_value");
                                        JSONObject jsonRag = new JSONObject(pgRag.getValue());
                                        try {
                                            if (jsonRag != null && jsonRag.has("functions")) {
                                                List<Long> idList = new ArrayList<Long>();
                                                JSONArray jsonArray = jsonRag.getJSONArray("functions");

                                                //우선 function call 용 LLM Parametger 노드 세팅 - RAG-API 수정후 삭제 예정
                                                if (jsonArray != null && jsonArray.length() > 0) {
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        idList.add(jsonArray.getLong(i));
                                                    }
                                                }

                                                //FunctioCall용 LLM 값 세팅
                                                setLLMNode(chatbotInfo, apiKeyMap, llmEngines,
                                                        Rag.ChatConfig.LLMNode.NodeIndex.FUNCTION_CALL, llmNodes, "{question}", disableNode);

                                                if (idList != null && idList.size() > 0) {
                                                    List<Map<String, Object>> functionList = chatbotInfoService.getFunctionInfoListByIds(idList);

                                                    for (Map<String, Object> item : functionList) {
                                                        String name = (String) item.get("name");
                                                        String description = (String) item.get("description");
                                                        String filterPrefix = (String) item.get("filter_prefix");
                                                        ;
                                                        if (filterPrefix == null || filterPrefix.isBlank()) {
                                                            filterPrefix = String.valueOf(chatbotId) + "_" + String.valueOf(item.get("id"));
                                                        }
                                                        List<Integer> preInfoTypeList = new ArrayList<Integer>();
                                                        Object preInfoType = item.get("pre_info_type");
                                                        if (preInfoType != null && preInfoType instanceof Integer[]) {
                                                            preInfoTypeList = Arrays.asList((Integer[]) preInfoType);
                                                        }
                                                        Rag.ChatConfig.Function function = Rag.ChatConfig.Function.newBuilder()
                                                                .setName(name)
                                                                .setDescription(description)
                                                                .setFilterPrefix(filterPrefix)
                                                                .addAllPreInfoType(preInfoTypeList)
                                                                .build();

                                                        functions.add(function);
                                                    }

                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                            sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:rag-function)");
                                            sink.complete();
                                            connectionHandler.connectionReset(chatroomId);
                                            chatScheduleTask.stop();
                                            return;
                                        }

                                        try {

                                            //rag노드 하위의 elastic 처리
                                            if (jsonRag != null && jsonRag.has("elastic_search")) {
                                                elasticSearch = jsonRag.getJSONObject("elastic_search");
                                                if (elasticSearch != null) {
                                                    int retry = 0;
                                                    int topK = 0;
                                                    Long endpoint = null;
                                                    ElasticConfig elasticParam = null;
                                                    List<EngineParam> params = null;
                                                    boolean useSparseVector = true;
                                                    boolean useDenseVector = true;
                                                    if (elasticSearch.has("retry"))
                                                        retry = elasticSearch.getInt("retry");
                                                    if (elasticSearch.has("top_k"))
                                                        topK = elasticSearch.getInt("top_k");
                                                    if (elasticSearch.has("endpoint")) {
                                                        endpoint = elasticSearch.getLong("endpoint");
                                                    }
                                                    if (elasticSearch.has("parameters")) {
                                                        EngineParamsConverter converter = new EngineParamsConverter();
                                                        params = converter.convertToEntityAttribute((String) elasticSearch.get("parameters").toString());
                                                        elasticParam = new ElasticConfig(params);
                                                    }
                                                    if (elasticSearch.has("use_sparse_vector")) {
                                                        useSparseVector = elasticSearch.getBoolean("use_sparse_vector");
                                                    }
                                                    if (elasticSearch.has("use_dense_vector")) {
                                                        useDenseVector = elasticSearch.getBoolean("use_dense_vector");
                                                    }


                                                    ElasticVO engine = engineService.getElasticEngineByIdWithMapper(endpoint);

                                                    esConfig = Rag.ChatConfig.ESConfig.newBuilder()
                                                            .setRetries(retry)
                                                            .setUrl(engine.getUrl())
                                                            .setIndex1(engine.getIndex1())
                                                            .setIndex2(engine.getIndex2())
                                                            .setApiKey(engine.getApik())
                                                            .setTopK(topK)
                                                            .setKnnK(elasticParam.getKnnK())
                                                            .setNumCandidates(elasticParam.getNumCandidates())
                                                            .setRrfRankConstant(elasticParam.getRrfRankConstant())
                                                            .setRrfSparseWeight(elasticParam.getRrfSparseWeight())
                                                            .setRrfDenseWeight(elasticParam.getRrfDenseWeight())
                                                            .setUseVectorReranker(elasticParam.getUseVectorReranker())
                                                            .setUseSparseVector(useSparseVector)
                                                            .setUseDenseVector(useDenseVector)
                                                            .build();
                                                }

                                            }

                                            if (jsonRag != null && jsonRag.has("embedding_engine_id")) {
                                                embeddingEngineId = jsonRag.getLong("embedding_engine_id");
                                            }
                                        } catch (Exception e) {
                                            log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                            sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:rag-elastic)");
                                            sink.complete();
                                            connectionHandler.connectionReset(chatroomId);
                                            chatScheduleTask.stop();
                                            return;
                                        }
                                    } catch (Exception e) {
                                        log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                                        sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (config-error:rag)");
                                        sink.complete();
                                        connectionHandler.connectionReset(chatroomId);
                                        chatScheduleTask.stop();
                                        return;
                                    }
                                    break;
                                /*
                                case ("function_call"):
                                    nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.FUNCTION_CALL;
                                    setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes);

                                    JSONObject functionCall = extractJSONFromItem(chatbotInfo);
                                    JSONArray jsonArray = functionCall.getJSONArray("functions");

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String name = jsonObject.getString("name");
                                        String description = jsonObject.getString("description");
                                        String filterPrefix = jsonObject.getString("filter_prefix");
                                        JSONArray preInfoType = jsonObject.getJSONArray("pre_info_type");

//                                        List<Rag.ChatConfig.PreInfo.Type> preInfoTypeList = new ArrayList<Rag.ChatConfig.PreInfo.Type>();
                                        List<Integer> preInfoTypeList = new ArrayList<Integer>();
                                        for (int idx = 0; idx < preInfoType.length(); idx++) {
//                                            preInfoTypeList.add(Rag.ChatConfig.PreInfo.Type.forNumber(preInfoType.getInt(idx)));
                                            preInfoTypeList.add(preInfoType.getInt(idx));
                                        }

                                        Rag.ChatConfig.Function function = Rag.ChatConfig.Function.newBuilder()
                                                .setName(name)
                                                .setDescription(description)
                                                .setFilterPrefix(filterPrefix)
                                                .addAllPreInfoType(preInfoTypeList)
                                                .build();

                                        functions.add(function);
//                                        // 각 JSONObject의 데이터를 처리
//                                        String name = jsonObject.getString("name");
//                                        int age = jsonObject.getInt("age");
//
//                                        // 처리 결과 출력
//                                        System.out.println("Name: " + name + ", Age: " + age);
                                    }


                                    break;
                                case ("elastic_search"):
                                    elasticSearch = extractJSONFromItem(chatbotInfo);
                                    int retry = 0;
                                    int topK = 0;
                                    Long endpoint = null;
                                    ElasticConfig elasticParam = null;
                                    List<EngineParam> params = null;
                                    boolean useSparseVector = true;
                                    boolean useDenseVector = true;
                                    if(elasticSearch.has("retry"))
                                        retry = elasticSearch.getInt("retry");
                                    if(elasticSearch.has("top_k"))
                                        topK = elasticSearch.getInt("top_k");
                                    if(elasticSearch.has("endpoint")) {
                                        endpoint = elasticSearch.getLong("endpoint");
                                    }
                                    if(elasticSearch.has("parameters")) {
                                        EngineParamsConverter converter = new EngineParamsConverter();
                                        params = converter.convertToEntityAttribute((String) elasticSearch.get("parameters").toString());
                                        elasticParam = new ElasticConfig(params);
                                    }
                                    if(elasticSearch.has("use_sparse_vector")) {
                                        useSparseVector = elasticSearch.getBoolean("use_sparse_vector");
                                    }
                                    if(elasticSearch.has("use_dense_vector")) {
                                        useDenseVector = elasticSearch.getBoolean("use_dense_vector");
                                    }


                                    ElasticVO engine = engineService.getElasticEngineByIdWithMapper(endpoint);

                                    esConfig = Rag.ChatConfig.ESConfig.newBuilder()
                                            .setRetries(retry)
                                            .setUrl(engine.getUrl())
                                            .setIndex1(engine.getIndex1())
                                            .setIndex2(engine.getIndex2())
                                            .setApiKey(engine.getApik())
                                            .setTopK(topK)
                                            .setKnnK(elasticParam.getKnnK())
                                            .setNumCandidates(elasticParam.getNumCandidates())
                                            .setRrfRankConstant(elasticParam.getRrfRankConstant())
                                            .setRrfSparseWeight(elasticParam.getRrfSparseWeight())
                                            .setRrfDenseWeight(elasticParam.getRrfDenseWeight())
                                            .setUseVectorReranker(elasticParam.getUseVectorReranker())
                                            .setUseSparseVector(useSparseVector)
                                            .setUseDenseVector(useDenseVector)
                                            .build();

                                    break;
                                 */
                            }

                        }
                    } else {
                        sink.next("오류가 발생했습니다. 잠시후 다시 사용해 주세요. (챗봇정보 없음:" + chatbotId + ")");
                        sink.complete();
                        connectionHandler.connectionReset(chatroomId);
                        chatScheduleTask.stop();
                        return;
                    }

                    String weatherInfo = null;
                    //날씨정보 Get
                    if (userKey != null && !userKey.isBlank()) {
                        MemberDetail memberDetail = memberDetailService.findMemberByUserKey(String.valueOf(userKey));
                        log.info("--------weather log(2)---------------" + memberDetail.getLatitude() + ":" + memberDetail.getLatitude());
                        if (memberDetail != null)
                            if(bWeatherInfo == false)
                                weatherInfo = "날씨정보 없음";
                            else {
                                weatherInfo = weatherService.callWeather(
                                        memberDetail.getLongitude() == null?weatherLongitude:memberDetail.getLongitude(),
                                        memberDetail.getLatitude() == null?weatherLatitude:memberDetail.getLatitude(),
                                        null, null);
                            }
                    }

                    if (weatherInfo != null && !weatherInfo.isBlank())
                        preInfoList.add(
                                Rag.ChatConfig.PreInfo.newBuilder()
//                                        .setType(Rag.ChatConfig.PreInfo.Type.WEATHER)
                                        .setIdx(4)
                                        .setData(weatherInfo)
                                        .build()
                        );

                    //default pre info - 임시코드 : 사용자 기본 정보와 날씨 정보를 default 값으로 지정.
                    List<Integer> defaultPreInfoList = Arrays.asList(0, 4);

                    //가져온 ChatInfo 기반으로 Config 생성

//                    EngineVO embeddingEngine = engineService.getEngineByIdWithMapper(1L); //일단 1번이 엠베딩 엔진으로 고정 (나중에 수정 필요)
                    EngineVO embeddingEngine = engineService.getEngineByIdWithMapper(embeddingEngineId);

                    Rag.APIKey embeddingApiKey = Rag.APIKey.newBuilder()
                            .setKey(embeddingEngine.getApik()==null?"":embeddingEngine.getApik())
                            .setVersion(embeddingEngine.getVersion()==null?"":embeddingEngine.getVersion())
                            .setEndpoint(embeddingEngine.getEndpoint()==null?"":embeddingEngine.getEndpoint())
//                            .setName(embeddingEngine.getName()==null?"":embeddingEngine.getName())
                            .setName(embeddingEngine.getName()==null?"":embeddingEngine.getModel())
                            .build();

                    Rag.ChatConfig chatConfig =
                            Rag.ChatConfig.newBuilder()
                                    .setRoomId(chatroomId)
                                    .addAllPreInfo(preInfoList)
                                    .setMemoryType(memoryType)
                                    .setMemoryWindow(memoryWindow)
                                    .addAllChatHistory(chatHistory)
                                    .addAllLlmEngines(llmEngines)
                                    .addAllLlmNodes(llmNodes)
                                    .setEmbeddingKey(embeddingApiKey)
                                    .setEsConfig(esConfig)
                                    .addAllFunctions(functions)
                                    .addAllDefaultPreInfo(defaultPreInfoList)
                                    .build();

                    Rag.ChatRequest requestWithConfig = Rag.ChatRequest.newBuilder()
                            .setConfig(chatConfig)
                            .setSequence(seq)
                            .build();

//                    log.info("requestWithConfig!!!!!:" + requestWithConfig.toString());
                    log.debug("[GRPC_CHAT] after Config info for LangChain & before request with Config!!!!");
//                    String logString = "[user_prompt]\n" + userPrompt.toString() + "\n";
                    requestObserver.onNext(requestWithConfig);
                    String configJsonString = JsonFormat.printer().print(requestWithConfig);
                    JSONObject jsonObjectConfig = new JSONObject(configJsonString);
                    JSONUtil.removeKey(jsonObjectConfig, "key");
                    JSONUtil.removeKey(jsonObjectConfig, "apiKey");
                    log.debug("grpc config\n" + jsonObjectConfig.toString());

                    String prettyjsonObjectConfig = JSONUtil.formatJSON(jsonObjectConfig);
                    monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "Send Config to LangChain", prettyjsonObjectConfig);

                }

                ChatRequest requestWithMessage =
                        ChatRequest.newBuilder()
                                .setMsg(userMsg)
                                .setSequence(seq)
                                .build();

                requestObserver.onNext(requestWithMessage);
                String logString = JsonFormat.printer().print(requestWithMessage);
                monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "Send Message to LangChain", logString);

            } catch (Exception e) {
                log.error("[GRPC_CHAT] Exception {}", e.getMessage());
                sink.next("오류가 발생했습니다. 잠시후 시도해 주세요. (ProtoChatService-Exception)");
                sink.complete();
                connectionHandler.connectionReset(chatroomId);
//                return;
            }
        });

//        fluxString.subscribe(
//                nextElement -> {
//                    log.debug("FLUX SUBSCRIBE.NEXTELEMENT:" + nextElement);
//                },
//                error -> {
//                    log.debug("FLUX SUBSCRIBE.ERROR:" + error.getMessage());
//                },
//                () -> {
//                    log.debug("FLUX SUBSCRIBE.COMPLETE!!!");
//                }
//        );

//        return fluxString;
    }

    /*
    public Flux<String> protoTest() {
        return Flux.create (sink -> {
                    StreamObserver<ChatRequest> requestObserver = ragServiceStub.chatHandler(new StreamObserver<ChatResponse>() {
                        @Override
                        public void onNext(ChatResponse chatResponse) {
                            // 서버로부터 응답을 받았을 때의 처리
                            System.out.println("protoTest Received response: " + chatResponse.getMsg());
                            sink.next(chatResponse.getMsg());
                        }

                        @Override
                        public void onError(Throwable t) {
                            // 에러 처리
                            System.out.println("protoTest error: " + t.getMessage());
                            t.printStackTrace();
                            sink.error(t);
                        }

                        @Override
                        public void onCompleted() {
                            // 스트림이 완료되었을 때의 처리
                            System.out.println("protoTest Stream completed!!!");
                            sink.complete();
//                channel.shutdownNow();
                        }
                    });

                    // Config 정보를 설정
                    String basePromptFormat = "You are a beauty consultant for a company called '아모레퍼시픽'. You NEVER create or involve in conversation in inappropriate topic like slang, bad words, racial, political. Your knowledge is limited to 아모레퍼시픽. You do not know anything about competing businesses, therefore you cannot answer any questions or conversation related to other businesses. IMPORTANT! DO NOT ASSUME PRODUCT NAME. WHEN THE USER IMPLIES THAT IS A PRODUCT, IT IS A PRODUCT. When you rewrite user's question, rewrite it in a way that you are actually asking the question.You can only use Korean.\"";
                    String rqaPromptFormat = "아래에 제시된 근거들을 참고하여, 기존 지식을 사용하지 말고 주어진 근거들만 사용해서 질문에 답변하라. 근거들을 통해 알맞은 답변을 찾을 수 없으면 \"해당 질문에 대해서는 답변이 불가능합니다. 죄송합니다.\"라고 답하라.";

                    APIKey llmKey = APIKey.newBuilder()
                            .setName("gpt-4-32k")
                            .setKey("d7cd56ee68b54f709132773194fe75d4")
                            .setVersion("2023-07-01-preview")
                            .setEndpoint("https://apaue-prd-openai.openai.azure.com/openai/deployments/gpt-4-32k/chat/completions?api-version=2023-07-01-preview")
                            .build();

                    APIKey embedKey = APIKey.newBuilder()
                            .setName("text-embedding-ada-002")
                            .setKey("564480d6e06544318894f47939ee12d1")
                            .setVersion("2022-12-01")
                            .setEndpoint("https://apcae-prd-openai.openai.azure.com/openai/deployments/text-embedding-ada-002/embeddings?api-version=2022-12-01")
                            .build();

                    LLMParameters llmParameters = LLMParameters.newBuilder()
                            .setKey(llmKey)
                            .setTemperature(1.0f)
                            .setTopP(0.5f)
                            .setPresencePenalty(1.0f)
                            .setFrequencyPenalty(1.0f)
                            .setPromptFormat(basePromptFormat)
                            .build();

                    LLMParameters rqaParameters = LLMParameters.newBuilder()
                            .setKey(llmKey)
                            .setTemperature(1.0f)
                            .setTopP(0.5f)
                            .setPresencePenalty(1.0f)
                            .setFrequencyPenalty(1.0f)
                            .setPromptFormat(rqaPromptFormat)
                            .build();

                    UserData userData = UserData.newBuilder()
                            .setType("")
                            .setData("")
                            .build();

                    Config config = Config.newBuilder()
                            .setRqaParams(llmParameters)
                            .setBaseParams(llmParameters)
                            .setMemoryType(Config.MemoryType.WINDOW_MEMORY)
                            .setMemoryWindow(10)
                            .setChatId("키값")
                            .setUserData(0, userData)
                            .build();

                    ChatRequest requestWithConfig = ChatRequest.newBuilder()
                            .setConfig(config)
                            .build();

                    requestObserver.onNext(requestWithConfig);

//                    List<String> docs = new ArrayList<String>();
//
//                    docs.add("\n아모레 시티랩을 방문하여 라이프스타일과 스킨케어 루틴 개선 방법 상담\n피부 상담 서비스는 마이 스킨 DNA 분석 완료 후, 예약하신 고객에 한해 진행 가능합니다.\n예약 진행 시 방문/전화 상담으로 선택하여 진행 가능합니다.\n방문 상담은 두피/피부 측정 후 피부 전문가와의 상담 순서로 진행되며, 약 90분 정도 소요 됩니다.\n방문 상담 장소는 \"서울 용산구 한강대로 100, 아모레퍼시픽 본사 2층 아모레 시티랩\" 입니다.\n전화 상담은 온라인 AI 피부 측정 후 피부 전문가와의 상담 순서로 진행되며, 약 60분 정도 소요 됩니다.\n\n<유전자 검사 서비스를 위한 회사별 역활>\n랩지노믹스는 \"키트생산 및 관리\", \"유전자 검사 분석 및 결과 제공\", \"유전자 검사 배송관리(협력사 CJ대한통운)\", \"고객 개인정보 관리\" 등 입니다.\n아모레퍼시픽은 판매업자로서 \"판매 상품 배송 및 유전자 분석 서비스 제공용 개인정보 관리\", \"마이 스킨 DNA 키트 제품정보 관리(아모레몰)\" 입니다.");
//                    docs.add("��전자 검사 의뢰는 제품 구매일로부터 1년 이내로 진행해 주시기 바랍니다.\n\n<주요 포인트>\n1. 피부부터 건강까지 69항목 검사\n2. 검체 채취 소요시간 단 1분\n3. 오프라인 피부 상담 연 4회 무료 진행\n\n<검사항목 69종>\n피부/모발 분야 : 색소 침착, 피부 노화, 기미/주근깨, 여드름 발생, 튼살/각질, 남성형 탙모, 모발 굵기, 원형 탈모, 새치\n영양과 대사 분야 : 비타민C, 비타민D, 코엔자임Q10, 마그네슘, 아연, 철, 칼륨, 칼슘, 아르기닌, 지방산, 비타민 B6, 비타민A, 비타민 E, 비타민 K, 비타민 B12, 베타인, 타이로신, 셀레늄, 루테인&지아잔틴, 글루타치온\n운동 분야 : 근력 운동 적합성, 유산소 운동 적합성, 지구력 운동 적합성, 근육 발달 운동, 단거리 질주 능력, 발목 부상 위험도, 악력, 운동 후 회복 능력\n건강 관리 분야 : 중성지방농도, 체질량지수, 콜레스테롤, 혈당, 혈압, 비만, 멀미, 골질량, 퇴행성 관절염증, 요산치,  체지방률, 복부비만, 체중감�\"");
//                    docs.add("서비스 소개\n해설 가이드\n피부/모발\n영양 및 대사\n건강관리\n개인특성\n운동\n식습관\n참고문헌분석 유전자 목록\n분석 안내서");
//                    docs.add("\"�� 입니다.\n아모레퍼시픽은 판매업자로서 \"판매 상품 배송 및 유전자 분석 서비스 제공용 개인정보 관리\", \"마이 스킨 DNA 키트 제품정보 관리(아모레몰)\" 입니다.  \"");
//                    docs.add("<진행 부서>\n아모레퍼시픽 > 아모레 시티랩\n\n<검사명>\n아모레 시티랩 마이 스킨 DNA\n\n<검사 비용>\n100,000원 (오프라인 상담 4회권 포함)\n\n<고객 피부 측정 건수>\n5,455건 이상\n\n<검사 방법>\n동봉 된 면봉으로 구강 내 샘플을 채취하여 튜브에 넣은 후 반송해 주시면 됩니다. \n\n<검사 결과>\n검사 결과지는 제공해 주신 이메일로 발송 드립니다.\n카카오톡을 통해 요약 보고서를 보내드립니다.\n요약 보고서 발송을 위해 카카오톡 채널 \"아모레 시티랩\"을 추가해 주세요\n\n<주의 사항>\n1. 본인 이외의 사용을 금합니다.\n2. 만 18세 이상만 검사가 가능합니다.\n3. 아모레몰 아이디당 1개만 구매 가능합니다.\n4. 본 검사는 질병의 진단 및 치료 목적으로 사용될 수 없으며, 의학적 소견이 필요한 경우 의사와 상담하시길 바랍니다.\n5. 유전자 검사 의뢰는 제품 구매일로부터 1년 이내로 진행해 주시기 바랍니다.\n\n<주요 포인트>\n1. 피부부터 건강까지 69항목 검사\n2. 검체 채취 소요시간 단 1분\n3. 오프라인 �\"");
//
//                    RelevantDocs relevantDocs = RelevantDocs.newBuilder().addAllDocs(docs).setDbType("SKINS").build();
//                    ClientMessage clientMessage = ClientMessage.newBuilder().setMsg("AI 피부 측정 서비스를 이용하려면 미리 예약을 해야 하나요?").addDocs(relevantDocs).build();
//                    ChatRequest requestWithMessage = ChatRequest.newBuilder().setMsg(clientMessage).build();
//
//                    requestObserver.onNext(requestWithMessage);

                }
        );

    }
    */

    /*
    @Deprecated
    public Flux<String> _startChat(ChatbotVO chatbot, Long chatroomId, String roomId, List<ChatMessage> messages) throws Exception {
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        Long seq = lastMessage.getSeq();
        String userMsg = lastMessage.getContent();

        monitorLogService.ChatMonitorLog(chatroomId, seq, "start:chatbotType:" + chatbot.getChatbotTypeCd());

        return Flux.create (sink -> {
            StreamObserver<ChatRequest> requestObserver = ragServiceStub.chatHandler(new StreamObserver<ChatResponse>() {
                @Override
                public void onNext(ChatResponse chatResponse) {
                    // 서버로부터 응답을 받았을 때의 처리
                    LogUtil.debug("protoTest Received response: " + chatResponse.getMsg());

                    Status status = chatResponse.getStatus();

                    LogUtil.debug("protoTest Received response: " + status!=null?status.getCodeValue():"");

                    if(status != null && status.getCode() == Status.Code.STREAM_END) {
                        LogUtil.debug("protoTest Stream completed!!!(onNext)");
                        chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete(onNext)");
                        sink.complete();
                    }

                    sink.next(chatResponse.getMsg());
                }

                @Override
                public void onError(Throwable t) {
                    // 에러 처리
                    LogUtil.debug("protoTest error: " + t.getMessage());
//                    t.printStackTrace();
                    chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Error:" + t.getMessage());
                    sink.error(t);
                }

                @Override
                public void onCompleted() {
                    // 스트림이 완료되었을 때의 처리
                    LogUtil.debug("protoTest Stream completed!!!");
                    chatService.sendSocketMessage(roomId, chatbot.getName(), "chat Complete");
                    sink.complete();
//                channel.shutdownNow();
                }
            });

            String basePromptFormat = chatbot.getPromptRole() + "\n" + chatbot.getPromptRequirement();
            String rqaPromptFormat = chatbot.getPromptTail();

            Long ragEngineId = Long.valueOf(chatbot.getRetrieverEngineId()==null?0:chatbot.getRetrieverEngineId());
            EngineVO ragEngine = engineService.getEngineByIdWithMapper(ragEngineId);

            Long llmEngineId = Long.valueOf(chatbot.getLlmEngineId()==null?0:chatbot.getLlmEngineId());
            EngineVO llmEngine = engineService.getEngineByIdWithMapper(llmEngineId);

            Long tailEngineId = Long.valueOf(chatbot.getTailEngineId()==null?0:chatbot.getTailEngineId());
            EngineVO tailEngine = engineService.getEngineByIdWithMapper(tailEngineId);

            if(ragEngine == null) {
                throw BaseException.of("RAG 엔진이 없습니다." + ragEngineId);
            } else if(llmEngine == null) {
                throw BaseException.of("LLM 엔진이 없습니다.(prompt1)" + llmEngineId);
            } else if(tailEngine == null) {
                throw BaseException.of("LLM 엔진이 없습니다.(prompt2)" + tailEngineId);
            }

            APIKey embedKey = APIKey.newBuilder()
                    .setEndpoint(ragEngine.getEndpoint())
                    .setKey(ragEngine.getApik())
                    .setName(ragEngine.getModel())
                    .setVersion(ragEngine.getVersion())
                    .build();
            APIKey basePromptKey = APIKey.newBuilder()
                    .setEndpoint(llmEngine.getEndpoint())
                    .setKey(llmEngine.getApik())
                    .setName(llmEngine.getModel())
                    .setVersion(llmEngine.getVersion())
                    .build();
            APIKey rqaPromptKey = APIKey.newBuilder()
                    .setEndpoint(tailEngine.getEndpoint())
                    .setKey(tailEngine.getApik())
                    .setName(tailEngine.getModel())
                    .setVersion(tailEngine.getVersion())
                    .build();

            OpenAIConfig llmConfig = new OpenAIConfig(chatbot.getLlmParameters());
            OpenAIConfig rqaConfig = new OpenAIConfig(chatbot.getTailParameters());

            LLMParameters llmParameters = LLMParameters.newBuilder()
                    .setKey(basePromptKey)
                    .setTemperature(llmConfig.getTemperature().floatValue())
                    .setTopP(llmConfig.getTopP().floatValue())
                    .setPresencePenalty(llmConfig.getPresencePenalty().floatValue())
                    .setFrequencyPenalty(llmConfig.getFrequencyPenalty().floatValue())
                    .setMaxTokens(llmConfig.getMaxTokens())
                    .setPromptFormat(basePromptFormat)
                    .build();

            LLMParameters rqaParameters = LLMParameters.newBuilder()
                    .setKey(rqaPromptKey)
                    .setTemperature(rqaConfig.getTemperature().floatValue())
                    .setTopP(rqaConfig.getTopP().floatValue())
                    .setPresencePenalty(rqaConfig.getPresencePenalty().floatValue())
                    .setFrequencyPenalty(rqaConfig.getFrequencyPenalty().floatValue())
                    .setMaxTokens(rqaConfig.getMaxTokens())
                    .setPromptFormat(rqaPromptFormat)
                    .build();

            Config config = Config.newBuilder()
                    .setBaseParams(llmParameters)
                    .setRqaParams(rqaParameters)
                    .addUserData(UserData.newBuilder().setType("personal").setData("{\"name\" : \"성진영\", \"age\" : \"30\", \"gender\" : \"male\"}").build())
                    .setChatId(String.valueOf(chatroomId))
                    .setMemoryType(Config.MemoryType.WINDOW_MEMORY)
                    .setMemoryWindow(10)
                    .build();

            ChatRequest requestWithConfig = ChatRequest.newBuilder()
                    .setConfig(config)
                    .build();
            requestObserver.onNext(requestWithConfig);

            ChatRequest requestWithMessage = ChatRequest.newBuilder().setMsg(userMsg).build();
            requestObserver.onNext(requestWithMessage);

        });
    }
     */
}
