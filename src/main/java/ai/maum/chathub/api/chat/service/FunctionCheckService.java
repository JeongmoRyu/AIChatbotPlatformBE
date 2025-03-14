package ai.maum.chathub.api.chat.service;

import ai.maum.chathub.api.admin.service.MonitorLogService;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.dto.EngineParamsConverter;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.mybatis.mapper.MonitorLogMapper;
import ai.maum.chathub.mybatis.vo.ElasticVO;
import ai.maum.chathub.mybatis.vo.EngineVO;
import ai.maum.chathub.mybatis.vo.MonitorLogVO;
import ai.maum.chathub.util.JSONUtil;
import ai.maum.chathub.api.chat.dto.ApiKeyInfo;
import ai.maum.chathub.api.chat.dto.elastic.ElasticConfig;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rag_service.rag_module.Rag;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FunctionCheckService {

    private final EngineService engineService;
    private final ChatbotInfoService chatbotInfoService;
    private final ProtoChatService protoChatService;
    private final MonitorLogMapper monitorLogMapper;
    private final MonitorLogService monitorLogService;

    @Value("${service.grpc.chat.host}")
    private String host;

    @Value("${service.grpc.chat.port}")
    private int port;
    @Value("${service.fn-test.chatbot-id}")
    private String fnTestChatbotId;

    @Value("${service.fn-test.embedding-id}")
    private Long fnTestEmbeddingId;

    public BaseResponse<Void> checkFunctionCall(Long functionId, String userMsg, MemberDetail user) throws Exception {
        log.debug("gRPC:createChannel For FunctionCall Test!!!" + host + ":" + port);
        log.debug("fntest:{}, {}", fnTestChatbotId, fnTestEmbeddingId);

        if(userMsg == null || userMsg.isBlank())
            userMsg = "펑션콜테스트입니다.";



            //grpc 채널 생성
        ManagedChannelBuilder channelBuilder = ManagedChannelBuilder.forAddress(host, port);
        if(port != 443)
            channelBuilder.usePlaintext();
        ManagedChannel channel = channelBuilder.build();

        rag_service.rag_module.RagServiceGrpc.RagServiceStub ragServiceStub = rag_service.rag_module.RagServiceGrpc.newStub(channel);

        if(ragServiceStub == null) {
            log.error("create GRPC Stub Create Error!!!");
            return BaseResponse.failure("GRPC 오류");
        }

//        Rag.ChatConfig chatConfig =
//                Rag.ChatConfig.newBuilder()
//                        .setRoomId(chatroomId)
//                        .addAllPreInfo(preInfoList)
//                        .setMemoryType(memoryType)
//                        .setMemoryWindow(memoryWindow)
//                        .addAllChatHistory(chatHistory)
//                        .addAllLlmEngines(llmEngines)
//                        .addAllLlmNodes(llmNodes)
//                        .setEmbeddingKey(embeddingApiKey)
//                        .setEsConfig(esConfig)
//                        .addAllFunctions(functions)
//                        .addAllDefaultPreInfo(defaultPreInfoList)
//                        .build();

//        Long chatbotId = 1L;
        Long chatbotId = Long.valueOf(fnTestChatbotId);
        List<Map<String, Object>> chatbotInfoMap = chatbotInfoService.getChatbotInfoMap(chatbotId);


        Long chatroomId = -1L;

        List<Rag.ChatConfig.PreInfo> preInfoList = new ArrayList<Rag.ChatConfig.PreInfo>();
        for(int i = 0 ; i <= 4; i++) {
            preInfoList.add(
                    Rag.ChatConfig.PreInfo.newBuilder()
                            .setIdx(i)
                            .setData("preInfo_" + i)
                            .build()
            );
        }

        Rag.ChatConfig.MemoryType memoryType = Rag.ChatConfig.MemoryType.WINDOW_MEMORY;

        int memoryWindow = 1;

        List<Rag.Chat> chatHistory = new ArrayList<Rag.Chat>();

        List<Rag.ChatConfig.LLMEngine> llmEngines = new ArrayList<Rag.ChatConfig.LLMEngine>();
        List<Rag.ChatConfig.LLMNode> llmNodes = new ArrayList<Rag.ChatConfig.LLMNode>();
        Map<Long, ApiKeyInfo> apiKeyMap = new HashMap<Long, ApiKeyInfo>();

//        EngineVO embeddingEngine = engineService.getEngineByIdWithMapper(1L);
        EngineVO embeddingEngine = engineService.getEngineByIdWithMapper(fnTestEmbeddingId);
//        Rag.APIKey embeddingApiKey = Rag.APIKey.newBuilder()
//                .setKey(embeddingEngine.getApik())
//                .setVersion(embeddingEngine.getVersion())
//                .setEndpoint(embeddingEngine.getEndpoint())
//                .setName(embeddingEngine.getName())
//                .build();

        Rag.APIKey embeddingApiKey = Rag.APIKey.newBuilder()
                .setKey(embeddingEngine.getApik()==null?"":embeddingEngine.getApik())
                .setVersion(embeddingEngine.getVersion()==null?"":embeddingEngine.getVersion())
                .setEndpoint(embeddingEngine.getEndpoint()==null?"":embeddingEngine.getEndpoint())
                .setName(embeddingEngine.getName()==null?"":embeddingEngine.getModel())
                .build();

        Rag.ChatConfig.ESConfig esConfig = Rag.ChatConfig.ESConfig.newBuilder().build();
        List<Rag.ChatConfig.Function> functions = new ArrayList<Rag.ChatConfig.Function>();
        List<Integer> defaultPreInfoList = Arrays.asList(0, 4);


        if (chatbotInfoMap != null && chatbotInfoMap.size() > 0) {
            for (Map<String, Object> chatbotInfo : chatbotInfoMap) {
                Rag.ChatConfig.LLMNode.NodeIndex nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.NULL;
                String key = (String) chatbotInfo.get("json_key");
                switch (key) {
                    case ("normal_conversation"):
                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.NORMAL_CHAT;
                        protoChatService.setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, false);
                        break;
                    case ("reproduce_question"):
                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.QUERY_REWRITE;
                        protoChatService.setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, false);
                        break;
                    case ("rag"):
                        List<Long> idList = new ArrayList<Long>();
                        idList.add(functionId);
                        nodeIndex = Rag.ChatConfig.LLMNode.NodeIndex.RAG_CHAT;
                        protoChatService.setLLMNode(chatbotInfo, apiKeyMap, llmEngines, nodeIndex, llmNodes, false);

                        PGobject pgRag = (PGobject) chatbotInfo.get("json_value");
                        JSONObject jsonRag = new JSONObject(pgRag.getValue());

                        protoChatService.setLLMNode(chatbotInfo, apiKeyMap, llmEngines,
                                Rag.ChatConfig.LLMNode.NodeIndex.FUNCTION_CALL, llmNodes, "{question}", false);
                        if(idList != null && idList.size() > 0) {
                            List<Map<String,Object>> functionList = chatbotInfoService.getFunctionInfoListByIds(idList);

                            for (Map<String,Object> item : functionList) {
                                String name = (String) item.get("name");
                                String description = (String) item.get("description");
                                String filterPrefix = (String) item.get("filter_prefix");;
                                if(filterPrefix == null || filterPrefix.isBlank()) {
                                    filterPrefix = String.valueOf(chatbotId) + "_" + String.valueOf(item.get("id"));
                                }
                                List<Integer> preInfoTypeList = new ArrayList<Integer>();
                                Object preInfoType = item.get("pre_info_type");
                                if(preInfoType != null && preInfoType instanceof Integer[]) {
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

                        if(jsonRag != null && jsonRag.has("elastic_search")) {
                            JSONObject elasticSearch = new JSONObject();
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

                        break;
                }
            }



        }


        rag_service.rag_module.Rag.ChatConfig chatConfig =
                rag_service.rag_module.Rag.ChatConfig.newBuilder()
                        .setRoomId(chatroomId)                          // 0 으로 고정
                        .addAllPreInfo(preInfoList)                     // function에서 가져옴
                        .setMemoryType(memoryType)
                        .setMemoryWindow(memoryWindow)
                        .addAllChatHistory(chatHistory)
                        .addAllLlmEngines(llmEngines)
                        .addAllLlmNodes(llmNodes)
                        .setEmbeddingKey(embeddingApiKey)
                        .setEsConfig(esConfig)
                        .addAllFunctions(functions)                     //function에서 가져옴
                        .addAllDefaultPreInfo(defaultPreInfoList)
                        .build();

        rag_service.rag_module.Rag.ChatRequest requestWithConfig = rag_service.rag_module.Rag.ChatRequest.newBuilder()
                .setConfig(chatConfig)
                .setSequence(0)
                .build();

        Long seq = System.currentTimeMillis();

        String configJsonString = JsonFormat.printer().print(requestWithConfig);
        JSONObject jsonObjectConfig = new JSONObject(configJsonString);
        JSONUtil.removeKey(jsonObjectConfig, "key");
        JSONUtil.removeKey(jsonObjectConfig, "apiKey");
        log.debug("grpc config\n" + jsonObjectConfig.toString());

        String prettyjsonObjectConfig = JSONUtil.formatJSON(jsonObjectConfig);
        monitorLogService.ChatMonitorLog(chatroomId, seq, "Send Config to LangChain", prettyjsonObjectConfig);

        Rag.ChatRequest requestWithMessage =
                Rag.ChatRequest.newBuilder()
                        .setMsg(userMsg)
                        .setSequence(seq)
                        .build();

        CompletableFuture<String> resultFuture = callGrpcServer(ragServiceStub, requestWithConfig, requestWithMessage);

        StringBuffer rtnStrBuffer = new StringBuffer();

        try {
            // 결과를 기다리고 출력
            String result = resultFuture.get();  // get()은 완료될 때까지 대기
            System.out.println("Final result: " + result);

            List<MonitorLogVO> logList = monitorLogMapper.selectLogForFunctionCallBySeq(seq);

            if(logList == null || logList.size() < 1) {
                rtnStrBuffer.append("FUNCTION CALL PROCESS NOT WORK!");
            } else {

                for (MonitorLogVO item : logList) {
                    String firstLine = ".";
                    // item.getLog()가 null이 아닌지 확인
                    if (item.getLog() != null && !item.getLog().trim().isEmpty()) {
                        // 로그를 줄 단위로 나누고, 빈 줄이 아닌 첫 번째 줄 찾기
                        String[] lines = item.getLog().split("\n");
                        for (String line : lines) {
                            if (!line.trim().isEmpty()) {  // 빈 줄(공백 포함) 제외
                                firstLine = line.trim();
                                break;
                            }
                        }
                    } else {
                        // 로그가 null이거나 빈 경우 예외 처리
                        log.warn("log is null or empty for item: " + item.getTitle());
                    }
                    log.debug("log:" + item.getTitle() + ":" + firstLine);
                    rtnStrBuffer.append(item.getTitle() + ":" + firstLine + "\n");
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return BaseResponse.success(null, rtnStrBuffer.toString());
    }

    public CompletableFuture<String> callGrpcServer( rag_service.rag_module.RagServiceGrpc.RagServiceStub ragServiceStub,
                                                     rag_service.rag_module.Rag.ChatRequest requestWithConfig,
                                                     Rag.ChatRequest requestWithMessage) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        StringBuffer stringBuffer = new StringBuffer();

        StreamObserver<Rag.ChatRequest> requestObserver = ragServiceStub.chatHandler(
                new StreamObserver<Rag.ChatResponse>() {
                    @Override
                    public void onNext(Rag.ChatResponse chatResponse) {
                        // 서버로부터 수신한 각 응답 메시지를 처리합니다.
                        log.info("Received response: " + chatResponse.getMsg());

                        stringBuffer.append(chatResponse.getMsg());
                        Rag.Status status = chatResponse.getStatus();
                        log.info("GRPC Received response: " + status + ":" + (status != null ? status.getCodeValue() : ""));
                        String answer = String.valueOf(stringBuffer.toString());

                        if (status != null && status.getCode() == Rag.Status.Code.STREAM_END) {
                            log.debug("GRPC Stream completed!!!(onNext)");
                            log.debug("GRPC Stream Sink Complete!!!(onNext) & ScheduleTask Stop");
                            resultFuture.complete(stringBuffer.toString());
                            stringBuffer.setLength(0);  //한턴이 끝나면 stringBuffer를 비워줌.
                        } else if (status != null && status.getCode().getNumber() >= 500) { //오류코드 500 이상일 경우
                            log.debug("GRPC Stream error!!!(onNext)");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        // 오류가 발생했을 때 처리
                        log.error("Error received: " + t.getMessage(), t);
                        resultFuture.completeExceptionally(t);
                    }

                    @Override
                    public void onCompleted() {
                        // 서버에서 스트림이 종료되었음을 알릴 때 처리
                        log.info("Stream completed.");
                        resultFuture.complete(stringBuffer.toString());
                    }
                }
        );

        requestObserver.onNext(requestWithConfig);
        requestObserver.onNext(requestWithMessage);

        return resultFuture;
    }

}
