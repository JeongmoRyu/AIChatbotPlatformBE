package ai.maum.chathub.api.chat.service;

import ai.maum.chathub.api.chat.dto.elastic.*;
import ai.maum.chathub.api.chat.dto.maumai.*;
import ai.maum.chathub.api.chatroom.dto.ChatroomDetail;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.entity.EngineEntity;
import ai.maum.chathub.api.engine.repo.EngineRepository;
import ai.maum.chathub.api.engine.service.EngineService;
import ai.maum.chathub.conf.interceptor.RequestResponseLoggingInterceptor;
import ai.maum.chathub.mybatis.vo.ChatbotVO;
import ai.maum.chathub.mybatis.vo.ChatroomDetailVO;
import ai.maum.chathub.mybatis.vo.EngineVO;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.util.ObjectMapperUtil;
import ai.maum.chathub.api.chat.handler.ChatSocketIOHandler;
import ai.maum.chathub.api.chat.dto.ChatMessage;
import ai.maum.chathub.api.chat.dto.openai.OpenAIChatMessage;
import ai.maum.chathub.api.chat.dto.openai.OpenAIRequest;
import ai.maum.chathub.api.chat.dto.openai.OpenAIResponse;
import ai.maum.chathub.api.admin.service.MonitorLogService;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.google.gson.*;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
//    @Value("${service.elastic.endpoint}")
    private String elasticUrl;

//    @Value("${service.elastic.key}")
    private String elasticKey;

//    @Value()
//    elastic:
//    endpoint: https://ai-platform-dev.es.vpce.ap-northeast-2.aws.elastic-cloud.com:9243

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final ChatSocketIOHandler chatSocketIOHandler;
    private final EngineRepository engineRepository;
    //    private final ChatroomRepository chatroomRepository;
    private final ChatroomService chatroomService;
    private final EngineService engineService;
    private final MonitorLogService monitorLogService;

    // openai api 직접 호출시 아래 코드 사용 - endpoint가 azure 일때 아래 코드 동작 한함. 확인 필요.
    // OpenAI 호출 메인
    @Transactional
    public Flux<String> getLlmCompletion(Object payload, EngineVO engine, String roomId, ChatbotVO chatbot, ChatroomDetailVO chatroomDetail) throws Exception {
        LogUtil.debug("getLlmCompletion!!!");
        HttpHeaders headers = new HttpHeaders();
//        EngineVO engine = engineService.getEngineByIdWithMapper(chatbot.getLlmEngineId());
//        String endpoint = engine.getEndpoint();
//        String apik = engine.getApik();
//        String vendor = engine.getVendor();

        // 서버에서 타밍아웃으로 인해 비정상 동작 -> 타임아웃 체크 로직 추가
        return checkConnection(engine.getEndpoint())
                .flatMapMany(valid -> {
                    LogUtil.debug("valid2:" + valid);
                    if (valid) {
                        LogUtil.debug("valid2:" + valid);
                        return sendRequest(payload, webClient, roomId, chatbot, chatroomDetail, engine);
                    } else {
                        LogUtil.debug("valid3:" + valid);
                        return recreateWebClient().flatMapMany(webClient -> sendRequest(payload, webClient, roomId, chatbot, chatroomDetail, engine));
                    }
                });
    }

    // 커넥션풀 타임아웃 체크.
    private Mono<Boolean> checkConnection(String endpoint) {
        // 여기에 커넥션이 유효한지 확인하는 로직을 작성
        // 유효한 경우 Mono.just(true) 반환
        // 유효하지 않은 경우 Mono.just(false) 반환
        return webClient.get()
                .uri(endpoint)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return Mono.just(true); // 유효한 커넥션으로 판단
                    } else {
                        return Mono.just(false); // 유효하지 않은 커넥션으로 판단
                    }
                })
                .onErrorResume(throwable -> {
                    // 요청 중 오류가 발생하면 유효하지 않은 커넥션으로 판단
                    return Mono.just(false);
                });
    }

    private Mono<WebClient> recreateWebClient() {
        // 새로운 WebClient 생성
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(100))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        WebClient newWebClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        return Mono.just(newWebClient);
    }

    @Transactional
    public Flux<String> sendRequest(Object payload, WebClient webClient, String roomId, ChatbotVO chatbot,
                                    ChatroomDetailVO chatroomDetail, EngineVO engine)  {
        StringBuilder strBuilder = new StringBuilder();
        LogUtil.info("sendRequest with timeout!!!!");

//        EngineEntity engine = engineRepository.getReferenceById(chatbot.getLlmEngineId());
        String authorization = "";
        String jsonPayload = "";
        String apik = engine.getApik();

        //endpoint가 azure일 경우 예외처리
        if(engine.getEndpoint() != null && engine.getEndpoint().indexOf("openai.azure.com") > 0) {
            authorization = "api-key";
        } else {
            authorization = HttpHeaders.AUTHORIZATION;
            apik = "Bearer " + engine.getApik();
        }

        try {
            jsonPayload = ObjectMapperUtil.writeValueAsString(payload);
        } catch (Exception e) {

        }

        LogUtil.info("sendRequest:endpoint:" + engine.getEndpoint());
        LogUtil.info("sendRequest:jsonPayload:" + jsonPayload);

        webClient.post()
                .uri(engine.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                .uri(engine.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(jsonPayload);

        if("OPENAI".equals(engine.getVendor()))
            requestSpec.header(authorization, apik);

        LogUtil.info("LLM PAYLOAD : " + jsonPayload);
        LogUtil.info("LLM CALL START");

        monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "LLM CALL:paylod", engine.getEndpoint() + ":" + jsonPayload);

        return requestSpec
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(3))
                        .filter(throwable -> throwable instanceof ReadTimeoutException))
                .onErrorResume(ReadTimeoutException.class, throwable -> {
                    // 타임아웃이 발생했을 때 처리할 내용을 여기에 작성
                    return Flux.empty(); // 예: 빈 Flux 반환
                })
                .flatMap(
                        response -> {
//                            LogUtil.debug(engine.getVendor() + ":response:" + response);
                            if("OPENAI".equals(engine.getVendor())) {
                                Gson gson = new Gson();
                                try {
                                    OpenAIResponse res = gson.fromJson(response, OpenAIResponse.class);
                                    List<OpenAIResponse.Choice> choices = res.getChoices();
                                    StringBuffer sbContents = new StringBuffer();

                                    if (choices != null) {
                                        for (OpenAIResponse.Choice choice : choices) {
                                            if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                                sbContents.append(choice.getDelta().getContent());
//                                                strContent.append(choice.getDelta().getContent());
                                                strBuilder.append(sbContents.toString());
                                            } else if (choice.getMessages() != null) { //버전에 따라서 choice 내부에 message로 conatents가  내려오는 경우도 있음.
                                                List<OpenAIResponse.Message> messages = choice.getMessages();
                                                for (OpenAIResponse.Message message : messages) {
                                                    if (message.getDelta() != null && message.getDelta().getContent() != null) {
                                                        sbContents.append(message.getDelta().getContent());
                                                        strBuilder.append(sbContents.toString());
//                                                        strContent.append(message.getDelta().getContent());
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        return Flux.just("");
                                    }
//                                    LogUtil.info("content1[" + sbContents.toString() + "]");
                                    return Flux.just(sbContents.toString());
                                } catch (Exception e) {
                                    return Flux.just("");
                                }
                            } else {
                                strBuilder.append(response);
                                return Flux.just(response);
                            }
                        })
                .doOnComplete(() -> {
                    LogUtil.info("LLM CALL END");

                    // 스트림이 성공적으로 완료되었을 때 실행할 로직
                    // 채팅룸에 title이 없을때는 채팅룸 Title을 업데이트 해줌.
                    // 일단 첫번째 질문으로 넣고 나중에 AI로 generate 해서 넣어 주자.
                    String answer = strBuilder.toString();
                    LogUtil.info("Stream processing complete!!!!!!:" + answer);
                    sendSocketMessage(roomId, chatbot.getName(), answer);

                    //RAG용 대답은 패스
                    if(!(answer.startsWith("[1,") ||  answer.startsWith("[2-1,") || answer.startsWith("[2-2,") || answer.startsWith("[3."))) {
                        setChatroomTitle(chatroomDetail, answer);
                        setChatRoomDetail(chatbot, chatroomDetail, answer);
                    }

                    monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "LLM ANSWER", answer);
                });
    }

    @Transactional
    public void setChatRoomDetail(ChatbotVO chatbot, ChatroomDetailVO chatroomDetailVO, String answer) {
        setChatRoomDetail(chatbot, chatroomDetailVO, answer, null);
    }

    @Transactional
    public void setChatRoomDetail(ChatbotVO chatbot, ChatroomDetailVO chatroomDetailVO, String answer, Long seq) {
//        Long seq = Long.valueOf(chatroomDetailVO.getSeq()); //(질문seq = 답변seq)

        if(seq == null || seq < 1L)
            seq = Long.valueOf(chatroomDetailVO.getSeq()); //(질문seq = 답변seq)

        String role = "assistant";

        LogUtil.debug("setChatRoom:chatbot:" + chatbot.getName() + ":" + chatbot.getId());
        LogUtil.debug("setChatRoom:chatroomId:" + chatroomDetailVO.getRoomId());
        LogUtil.debug("setChatRoom:answer:" + answer);

        LogUtil.debug("before insert chatroom detail...");
        ChatroomDetailEntity chatroomDetail = new ChatroomDetailEntity(chatroomDetailVO.getRoomId(), seq, role, answer);
        chatroomDetail = chatroomService.setChatroomDetail(chatroomDetail);
        LogUtil.debug("after insert chatroom detail...:" + ObjectMapperUtil.writeValueAsString(chatroomDetail));
        monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "Chat bot Answer", answer);
    }

    @Transactional
    public void setChatroomTitle(ChatroomDetailVO chatroomDetailVO, String answer) {
        if (chatroomDetailVO != null || chatroomDetailVO.getRoomId() != null
                || chatroomDetailVO.getRoomId() > 0) {
//
            if (chatroomDetailVO.getRoomId() != null && chatroomDetailVO.getRoomId() > 0) {
                String title = answer != null ? answer.substring(0, answer.length() > 20 ? 20 : answer.length() - 1) : null;
                chatroomService.setChatroomTitle(chatroomDetailVO.getRoomId(), title);
            }
        }
    }

    public void sendSocketMessage(String roomId, String title, String content) {
        try {
            if (roomId == null || roomId.isEmpty()) {
                log.debug("roomId is invalid:{}", roomId);
            } else {
                chatSocketIOHandler.sendMessageToSession(roomId, title, content);
            }
        } catch (Exception e) {
//            log.error(e.getMessage());
            log.debug("sendSocketMessage:" + e.getMessage());
        }
    }

    private String processResponseAzureWithListAsString(ChatCompletions chatCompletions) {
        StringBuffer sbContents = new StringBuffer();

        List<ChatChoice> choices = chatCompletions.getChoices();

        if (choices != null) {
            for (ChatChoice choice : choices) {
                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    String strContent = choice.getDelta().getContent().toString();
                    if (strContent != null && !strContent.isEmpty() && !strContent.equals("null")) {
                        sbContents.append(strContent);
                        LogUtil.debug("strContent:" + strContent);
                    }
                }
            }
        }
        return sbContents.toString();
    }

    @Transactional
    public Flux<String> sendLlmChat(ChatbotVO chatbot, ChatroomDetailVO chatroomDetail , String roomId,
                                    List<OpenAIChatMessage> messages, String processType, List<String> ragResult) throws Exception {
        if(chatbot == null) {
            LogUtil.info("chatbot is null");
            throw BaseException.of("챗봇이 없습니다.");
        }
        else if(chatbot.getLlmEngineId() < 1) {
            LogUtil.info("llm engine id:" + chatbot.getLlmEngineId());
            throw BaseException.of("해당 챗봇에 설정된 엔진정보가 없습니다.");
        } else if ("AMR".equals(chatbot.getChatbotTypeCd()) && (chatbot.getTailEngineId() != null && chatbot.getTailEngineId() < 1)) {
            LogUtil.info("tail engine id:" + chatbot.getTailEngineId());
            throw BaseException.of("해당 챗봇에 설정된 두번째 프롬프트의 엔진정보가 없습니다.");
        }

        Long llmEngineId = Long.valueOf(chatbot.getLlmEngineId()==null?0:chatbot.getLlmEngineId());
        EngineVO llmEngine = engineService.getEngineByIdWithMapper(llmEngineId);

        Long tailEngineId = Long.valueOf(chatbot.getTailEngineId()==null?0:chatbot.getTailEngineId());
        EngineVO tailEngine = engineService.getEngineByIdWithMapper(tailEngineId);

        String engineName = llmEngine.getName();
        EngineVO engine = llmEngine;

        if("AMR".equals(chatbot.getChatbotTypeCd()) && "POST".equals(processType)) {
            if(tailEngine == null) {
                LogUtil.info("chatbot is null");
                throw BaseException.of("LLM(TAIL) 엔진이 없습니다." + llmEngineId);
            }
            else if(tailEngine.getName() == null || tailEngine.getName().isEmpty()) {
                LogUtil.info("tail-llm engine name error" + chatbot.getTailEngineId());
                throw BaseException.of("LLM(TAIL) 엔진정보 오류 입니다.");
            }
            /* 수정필요!!! - 프론트작업 끝날때 까지는 일단 prompt 1 으로만 동작. - 프론트 작업 완료에 따른 주석처리.*/
            engineName = tailEngine.getName();
            engine = tailEngine;
        } else {
            if(llmEngine == null) {
                LogUtil.info("chatbot is null");
                throw BaseException.of("LLM 엔진이 없습니다." + llmEngineId);
            }
            else if(llmEngine.getName() == null || llmEngine.getName().isEmpty()) {
                LogUtil.info("llm engine name error" + chatbot.getLlmEngineId());
                throw BaseException.of("LLM 엔진정보 오류 입니다.");
            }
            engineName = llmEngine.getName();
            engine = llmEngine;
        }

        Object payload = new Object();

        if("OPENAI".equals(engine.getVendor())) {
            payload = setPayLoadOpenAILlm(chatbot, messages, processType, ragResult);
            OpenAIRequest openAIRequest = (OpenAIRequest)payload;
            sendSocketMessage(roomId, engineName, ObjectMapperUtil.writeValueAsString(openAIRequest));
        } else if("MAUMAI".equals(engine.getVendor())) {
            payload = setPayLoadMaumAI(chatbot,llmEngineId, messages);
            MaumAIRequest maumAIRequest = (MaumAIRequest)payload;
            sendSocketMessage(roomId, engineName, ObjectMapperUtil.writeValueAsString(maumAIRequest.getParam()));
        }

        return getLlmCompletion(payload, engine, roomId, chatbot, chatroomDetail);
    }

    private OpenAIRequest setPayLoadOpenAILlm(ChatbotVO chatbot, List<OpenAIChatMessage> messages,
                                              String processType, List<String> ragResult) {

        LogUtil.debug("setPayLoadOpenAILlm.messages:" + ObjectMapperUtil.writeValueAsString(messages));

        OpenAIRequest payload = new OpenAIRequest();

        List<EngineParam> engineParam = chatbot.getLlmParameters();

        /* 수정필요!!! - 프론트작업 끝날때 까지는 일단 prompt 1 으로만 동작. - 프론트 작업 완료에 따른 주석처리.*/
        if("POST".equals(processType))
            engineParam = chatbot.getTailParameters();

        for (EngineParam param : engineParam) {
            Double value = Double.valueOf(param.getValue());
            switch (param.getKey()) {
                case ("top_p"):
                    payload.setTopP(value);
                    break;
//                    case ("top_k"):
//                        payload.setTopK(value);
//                        break;
                case ("temp"):
                    payload.setTemperature(value);
                    break;
                case ("pres_p"):
                    payload.setPresencePenalty(value);
                    break;
                case ("freq_p"):
                    payload.setFrequencyPenalty(value);
                    break;
                case ("max_token"):
                    payload.setMaxTokens((int) value.doubleValue());
            }
        }

        if (payload.getMaxTokens() == null || payload.getMaxTokens() < 1)
            payload.setMaxTokens(4096);

        EngineVO engine = engineService.getEngineByIdWithMapper(chatbot.getLlmEngineId());
        String model = engine.getModel();

        payload.setModel(model);
        payload.setStream(true);

        String requirement = chatbot.getPromptRole() + "\n" + chatbot.getPromptRequirement();

        //마지막 user message 추출
        String userMessage = "";
        for(OpenAIChatMessage item: messages) {
            String role = item.getRole();
            if(role != null)
                role = role.toUpperCase();
            if("USER".equals(role))
                userMessage = item.getContent();
        }


        if ("PRE".equals(processType)) {
            List<OpenAIChatMessage> msgs = new ArrayList<OpenAIChatMessage>();
            requirement = chatbot.getPromptRole() + "\n" + chatbot.getPromptRequirement();
            msgs.add(new OpenAIChatMessage("system", requirement));
            msgs.add(new OpenAIChatMessage("user", userMessage));
            payload.setMessages(msgs);
        }   else if ("POST".equals(processType)) {
            List<OpenAIChatMessage> msgs = new ArrayList<OpenAIChatMessage>();
            requirement = chatbot.getPromptTail();
            //POST일때는 user에  정보도 넣어줌.
            msgs.add(new OpenAIChatMessage("system", requirement));
            String userPrompt = "";
            if(ragResult != null && ragResult.size() > 0) {
                for(int i = 0 ; i < ragResult.size() ; i++) {
                    userPrompt += "\"정보\" [" + (i+1) + "] :" + ragResult.get(i) + "\n\n";
                }
                userPrompt += "질문: " + userMessage;
            }
            msgs.add(new OpenAIChatMessage("user", userPrompt));
            payload.setMessages(msgs);

            LogUtil.info("PAYLOAD:" + ObjectMapperUtil.writeValueAsString(payload));


        } else {
            //일반적인 경우
            OpenAIChatMessage prompt = new OpenAIChatMessage("system", requirement);
            LogUtil.info("PROMPT!!!!!:" + ObjectMapperUtil.writeValueAsString(prompt));
            messages.add(0, prompt);
            payload.setMessages(messages);
            LogUtil.info("PAYLOAD:" + ObjectMapperUtil.writeValueAsString(payload));
        }
//        OpenAIChatMessage prompt = new OpenAIChatMessage("system", chatbot.getPromptRole() + "\n" + chatbot.getPromptRequirement());
        return payload;
    }

    private MaumAIRequest setPayLoadMaumAI(ChatbotVO chatbot, Long engineId, List<OpenAIChatMessage> messages) {

        //RAG,LLM 공통
        MaumAIRequest payload = new MaumAIRequest();

        EngineVO engine = engineService.getEngineByIdWithMapper(engineId);

        payload.setAppId(String.valueOf(engine.getApik()));
        payload.setName(String.valueOf(engine.getModel()));

        List<String> items = new ArrayList<String>();
        items.add(engine.getModel());

        payload.setItem(items);

        if("RAG".equals(chatbot.getChatbotTypeCd())) {
            MaumAIParam params = new MaumAIParam();
            messages.forEach(message -> {
                switch (message.getRole()) {
                    case "user":
                        //가장 마지막 user message 만 넣어주면 됨
                        params.setUtterance(message.getContent());
                        break;
                }
            });

            List<EngineParam> ragParam = chatbot.getRagParameters();

            if(ragParam != null && ragParam.size() > 0) {
                for (EngineParam param : ragParam) {
                    Double value = Double.valueOf(param.getValue());
                    switch (param.getKey()) {
                        case ("num_items"):
                            params.setNumItems(value.intValue());
                            break;
                    }
                }
            }

            try {
                Object[] additionalParams = new Gson().fromJson(engine.getParametersAdditional(), Object[].class);
                if(additionalParams.length > 0) {
                    for (Object param : additionalParams) {
                        Map<String, Object> map = (Map<String, Object>) param;
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
//                            System.out.println("key: " + entry.getKey());
//                            System.out.println("value: " + entry.getValue());
                            switch(entry.getKey().toLowerCase()) {
                                case ("agent_id"):
                                    params.setAgentId((String)entry.getValue());
                                    break;
                                case ("conditions"):
                                    params.setConditions((List<String>)entry.getValue());
                                    break;
                            }
                        }
                    }
                }
                params.getNumItems();
            } catch (Exception e) {
                LogUtil.error(e.getMessage());
            }

            List<MaumAIParam> paramList = new ArrayList<MaumAIParam>();
            paramList.add(params);
            payload.setParam(paramList);

            return payload;
        } else {
            List<OpenAIChatMessage> utterances = new ArrayList<OpenAIChatMessage>();

            OpenAIChatMessage prompt = new OpenAIChatMessage("ROLE_SYSTEM", chatbot.getPromptRole() + "\n" + chatbot.getPromptRequirement());
            utterances.add(prompt);

            messages.forEach(message -> {
                switch (message.getRole()) {
                    case "system":
                        utterances.add(new OpenAIChatMessage("ROLE_SYSTEM", message.getContent()));
                        break;
                    case "user":
                        utterances.add(new OpenAIChatMessage("ROLE_USER", message.getContent()));
                        break;
                    case "assistant":
                        utterances.add(new OpenAIChatMessage("ROLE_ASSISTANT", message.getContent()));
                        break;
                }
            });

            MaumAIParam params = new MaumAIParam();
            params.setUtterances(utterances);

            MaumAIConfig config = new MaumAIConfig();
            List<EngineParam> llmParam = chatbot.getLlmParameters();
            if(llmParam != null && llmParam.size() > 0) {
                for (EngineParam param : llmParam) {
                    Double value = Double.valueOf(param.getValue());
                    switch (param.getKey()) {
                        case ("top_p"):
                            config.setTopP(value);
                            break;
                        case ("top_k"):
                            config.setTopK(value);
                            break;
                        case ("temp"):
                            config.setTemperature(value);
                            break;
                        case ("pres_p"):
                            config.setPresencePenalty(value);
                            break;
                        case ("freq_p"):
                            config.setFrequencyPenalty(value);
                            break;
                        case ("penalty_alpha"):
                            config.setPenalty_alpha(value);
                            break;
                    }
                }
            }

            //Llma2 Base 모델은 config의 키 값이 "generation_config" 라서 예외 처리
            //나머지는 그냥 "config"
            if("maumgpt-ko-llama2-streamchat".equals(engine.getModel()))
                params.setGenerationConfig(config);
            else
                params.setConfig(config);

            List<MaumAIParam> paramList = new ArrayList<MaumAIParam>();

            paramList.add(params);
            payload.setParam(paramList);

            return payload;
        }
    }

//    private Flux<String> preProcess (ChatbotEntity chatbot, ChatroomDetailEntity chatroomDetail,
//                                     String roomId, List<OpenAIChatMessage> openAiChatMessages) {
//
//
//
//
//        return Flux.just("ok");
//    }

    @Transactional
    public Flux<String> startChat(ChatbotVO chatbot, Long chatroomId, String roomId, List<ChatMessage> messages) throws Exception {
        //message에서 질문 + seq를 가져와야 함.
        //messages의 제일 마지막에 있는게 현재 질문.
        //마지막 message에서 질문 내용 추출
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String content = lastMessage.getContent();
        String role = lastMessage.getRole();
        Long seq = lastMessage.getSeq();

        if(seq == null) {
            //seq가 null이면 seq generate. 겹칠수도 있으나 일단 go..
            seq = chatroomService.getChatroomSeq(chatroomId);
        }

        LogUtil.debug("before insert chatroom detail...");
        //질문내용을 chatroom detail에 넣어 줌.
        ChatroomDetail chatroomDetail = new ChatroomDetail(chatroomId, seq, role, content);
        LogUtil.debug("after insert chatroom detail...:" + ObjectMapperUtil.writeValueAsString(chatroomDetail));
        ChatroomDetailEntity chatroomDetailEntity = chatroomService.setChatroomDetail(new ChatroomDetailEntity(chatroomId, seq, role, content));
        Long chatroomDetailId = chatroomDetailEntity.getId();
        ChatroomDetailVO chatroomDetailVO= chatroomService.getChatRoomDetailByIdFromMapper(chatroomDetailId);

        //messages 를 ChatMessage -> OpenAIChatMessage로 변경
        List<OpenAIChatMessage> openAiChatMessages = new ArrayList<OpenAIChatMessage>();
        for(ChatMessage item : messages)
            openAiChatMessages.add(new OpenAIChatMessage(item));

        LogUtil.debug("CHATBOT TYPE : " + chatbot.getChatbotTypeCd());

        monitorLogService.ChatMonitorLog(chatroomId, seq, "start:chatbotType", chatbot.getChatbotTypeCd());

        // RAG,LLM 분기처리
        // AMR은 LLM+RAG+LLM이 섞여 있는 구조 이므로 별도로 처리
        if("AMR".equals(chatbot.getChatbotTypeCd())) {
            StringBuilder sbAnswer = new StringBuilder();
            return sendLlmChat(chatbot, chatroomDetailVO, roomId,  openAiChatMessages, "PRE", null)//첫번째 LLM 프로세스.
                    .collectList()
                    .flatMapMany( list -> {
                        for (String item : list) {
//                            LogUtil.debug("preprocess:" + item);
                            sbAnswer.append(item);
                        }

                        String strAnswer = sbAnswer.toString();
                        LogUtil.debug("Pre Answer:" + strAnswer);
//                        monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "LLM ANSWER(AMR):" + strAnswer);


                        //제일 앞과 끝의 [] 제거
                        //제일 먼저 나오는 [와 나중에 나오는 ]제거 하고 호출
//                        strAnswer = strAnswer.replaceAll("^\\[|\\]$", "");
                        //그 후 제일 먼저 나오는 "를 제거
//                        strAnswer = strAnswer.replaceAll("^\"\"$", "");

//                        String[] parseString = strAnswer.split(",");
                        String vectorIndex = "aabc-vectordb";
//                        String vectorIndex = "aabc-mcl-personal";
//                        String vectorIndex = "aabc-mcl-wiki";

                        if(strAnswer.startsWith("[1,") || strAnswer.startsWith("[\"1,")) {
                            LogUtil.debug("Pre Process: 1!!!");
                            vectorIndex = "aabc-mcl-wiki";
                        } else if(strAnswer.startsWith("[2-1,") || strAnswer.startsWith("[\"2-1,")) {
                            LogUtil.debug("Pre Process: 2-1!!!");
                            vectorIndex = "aabc-mcl-personal";
                        } else if(strAnswer.startsWith("[2-2,") || strAnswer.startsWith("[\"2-2,")) {
                            LogUtil.debug("Pre Process: 2-2!!!");
                            vectorIndex = "aabc-mcl-personal";
                        } else if(strAnswer.startsWith("[3,") || strAnswer.startsWith("[\"3,")) {
                            LogUtil.debug("Pre Process: 3!!!");
                            vectorIndex = "aabc-vectordb";
                        } else {
                            LogUtil.debug("Pre Process pass!!!");
                            return Flux.just(strAnswer);
                        }

                        vectorIndex = "aabc-mcl-wiki";// 우선 고정

                        strAnswer = strAnswer.replaceAll("^\\[|\\]$", "");
                        String[] parseString = strAnswer.split(",");

                        List<String> ragResult = new ArrayList<String>();

                        if(parseString != null && parseString.length > 0) {
                            //i = 0 인 경우는 index 이므로 1 부터 시작
                            for (int i = 1; i < parseString.length ; i++ ) {
                                List<String> ragResultTmp = new ArrayList<String>();

                                //제일 먼저 나오는 "와 나중에 나오는 " 제거 하고..
                                String question = parseString[i].trim().replaceAll("^\\\"|\\\"$", "");
                                //숫자로만 되어 있으면 순번이므로 패스
                                if(!question.matches("^[0-9]+$")) {
                                    ragResultTmp = openAIEmbeddingAndRagSearch(chatbot, chatroomDetailVO, question, vectorIndex);

                                    if(ragResultTmp != null && ragResultTmp.size() > 0)
                                        ragResult.addAll(ragResultTmp);                                }
                            }
                        }

                        LogUtil.debug("RagResult:" + ObjectMapperUtil.writeValueAsString(ragResult));

                        //RAG결과과 없으면 일반 프롬프트로...
                        if(ragResult == null || ragResult.size() < 1) {

                            try {
                                chatbot.setChatbotTypeCd("LLM");
                                return sendLlmChat(chatbot, chatroomDetailVO, roomId, openAiChatMessages, null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return Flux.just("오류가 발생했습니다.");
                            }
                        }

                        try {
                            return sendLlmChat(chatbot, chatroomDetailVO, roomId,  openAiChatMessages, "POST", ragResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Flux.just("오류가 발생했습니다.");
                        }

                    });

        } else if("RAG".equals(chatbot.getChatbotTypeCd())) {
            EngineEntity engine = engineRepository.getReferenceById(chatbot.getRetrieverEngineId());
//            EngineEntity engine = chatbot.getRetrieverEngine();
            // OPENAI일때는 embedding. engine은 일단 text-embedding-ada-002로 고정
            if(engine != null && "OPENAI".equals(engine.getVendor())) {
                List<String> ragResult = openAIEmbeddingAndRagSearch(chatbot, chatroomDetailVO, content, null);
                //Chat Message 구성후 OPENAI는 바로 LLMChat으로 보냄.

//                String llmPrompt = chatbot.getPromptRequirement() + "\n" +
//                        "정보는 [{\"score\":0.98, \"text\":\"정보1\"}, {\"score\":0.98, \"text\":\"정보1\"}] 형태의 json list로 구성 되어 있어\n" +
//                        "점수가 높을수록 신뢰도가 있고 답변은 text의 내용을 기반으로 하면 되.";

                String llmPrompt = chatbot.getPromptRequirement() + "\n";

                if(ragResult.size() > 0) {
                    llmPrompt += "다음에 오는 정보들을 기반으로 대답해줘. ";
                    for (int i = 1; i <= ragResult.size(); i++) {
                        llmPrompt += "[정보" + i + "]\n" + ragResult.get(i-1) + "\n";
                    }
                }

                chatbot.setChatbotTypeCd("LLM");
                chatbot.setPromptRequirement(llmPrompt);
                return sendLlmChat(chatbot, chatroomDetailVO, roomId,  openAiChatMessages, null, null);
            } else
                return sendRagChat(chatbot, chatroomDetailVO, roomId,  openAiChatMessages);

        } else {
            return sendLlmChat(chatbot, chatroomDetailVO, roomId,  openAiChatMessages, null, null);
        }
    }

    private List<String> openAIEmbeddingAndRagSearch(ChatbotVO chatbot, ChatroomDetailVO chatroomDetail, String content, String vectorIndex) {
        List<String> ragResult = new ArrayList<String>();

        Long engineId = chatbot.getRetrieverEngineId();
        if(engineId == null || engineId < 0L)
            return ragResult;

        EngineVO engine = engineService.getEngineByIdWithMapper(engineId);
        if(engine == null)
            return ragResult;

//        String endpoint = "https://apcae-prd-openai.openai.azure.com/openai/deployments/text-embedding-ada-002/embeddings?api-version=2022-12-01";
//        String key = "564480d6e06544318894f47939ee12d1";
        String endpoint = engine.getEndpoint();
        String key = engine.getApik();

        List<EngineParam> engineParam = chatbot.getRagParameters();

        Integer topK = null;
        Double maxTokens = null;

        for (EngineParam param : engineParam) {
            Double value = Double.valueOf(param.getValue());
            switch (param.getKey()) {
                case ("top_k"):
                    topK = (int)value.doubleValue();
                    break;
                case ("max_token"):
                    maxTokens = value;
            }
        }

        String jsonPayload = "{\"input\": \"" + content + "\", \"max_tokens\": " + maxTokens + "}";
        StringBuffer sbContents = new StringBuffer();

        try {

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("api-key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            LogUtil.info("EMBEDDING PAYLOAD : " + jsonPayload);

            HttpRequest request = requestBuilder.build();

            LogUtil.info("EMBEDDING START");

            monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(),
                    "QUESTION EMBEDDING" , endpoint + ":" + jsonPayload);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LogUtil.info("EMBEDDING COMPLETE.1");

            JSONObject jsonObject = new JSONObject(response.body());
//            monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "QUESTION EMBEDDING:RESULT:" + ObjectMapperUtil.writeValueAsString(jsonObject));
            JSONArray jsonArray = jsonObject.getJSONArray("data").getJSONObject(0).getJSONArray("embedding");
            List<Double> embeddingList = jsonArrayToList(jsonArray);

            if(embeddingList == null || embeddingList.size() < 1)
                LogUtil.info("EMBEDDING COMPLETE.2: BUT NO RESULT");


            ragResult = callElastic(chatroomDetail, vectorIndex, content, embeddingList, topK);

        } catch (Exception e) {
            LogUtil.error("openAIEmbeddingAndRagSearch:Exception:" + e.getMessage() );
        }
        return ragResult;
    }

    private List<Double> jsonArrayToList(JSONArray jsonArray) {
        return IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::getDouble) // JSONArray에서 Double 추출
                .collect(Collectors.toList()); // List로 수집
    }

    private List<String> callElastic(ChatroomDetailVO chatroomDetail, String vectorIndex, String userQuestion, List<Double> userVector, Integer topK) {

        List<String> ragResult = new ArrayList<String>();

        ElasticQueryData queryObject = new ElasticQueryData();

        ElasticQuery elasticQuery = new ElasticQuery();
        ElasticQuery.Term elasticQueryTerm = elasticQuery.new Term();
        elasticQueryTerm.setText(userQuestion);
        elasticQuery.setTerm(elasticQueryTerm);
        queryObject.setQuery(elasticQuery);

        ElasticKnn elasticKnn = new ElasticKnn();
        elasticKnn.setField("vector");
        elasticKnn.setQuery_vector(userVector);
        elasticKnn.setK(topK==null?5:topK);
        elasticKnn.setNum_candidates(10);
        queryObject.setKnn(elasticKnn);

        ElasticRank elasticRank = new ElasticRank();
        ElasticRank.ElasticRankRrf elasticRankRrf = elasticRank.new ElasticRankRrf();
        elasticRankRrf.setWindow_size(10);
        elasticRankRrf.setRank_constant(1);
        elasticRank.setRrf(elasticRankRrf);
        queryObject.setRank(elasticRank);

        queryObject.setSize(5);

        ElasticAggs elasticAggs = new ElasticAggs();
        ElasticAggs.IntCount elasticAggsIntCount = elasticAggs.new IntCount();
        ElasticAggs.IntCount.Terms elasticAggsIntCountTerms = elasticAggsIntCount.new Terms();
        elasticAggsIntCountTerms.setField("integer");
        elasticAggsIntCount.setTerms(elasticAggsIntCountTerms);
        elasticAggs.setInt_count(elasticAggsIntCount);
        queryObject.setAggs(elasticAggs);

//        new Gson().fromJson(userVector, List<Double>.class);
//        elasticKnn.setQuery_vector();


//        String elasticsearchUrl = "http://localhost:9200/your_index/_search"; // Elasticsearch 인덱스 URL
//       String searchUrl = elasticUrl + "/aabc-vectordb/_search"; // Elasticsearch 인덱스 URL
//        String searchUrl = elasticUrl + "/aabc-mcl-personal/_search"; // Elasticsearch 인덱스 URL

        if(vectorIndex == null || vectorIndex.isEmpty())
            vectorIndex = "aabc-mcl-wiki";

        String searchUrl = elasticUrl + "/" + vectorIndex + "/_search"; // Elasticsearch 인덱스 URL
//        String queryJson = "{\"query\": {\"match_all\": {}}}"; // Elasticsearch 쿼리 JSON 문자열
        String queryJson = ObjectMapperUtil.writeValueAsString(queryObject);

        LogUtil.info("RAG SEARCH PAYLOAD : " + queryJson);

        //로깅시는 knn의 queryvector는제외
        ElasticKnn elasticKnn4Log = queryObject.getKnn();
        elasticKnn4Log.setQuery_vector(null);
        queryObject.setKnn(elasticKnn4Log);

        monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "RAG SEARCH", searchUrl + ":" + ObjectMapperUtil.writeValueAsString(queryObject));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(searchUrl);

        try {
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "ApiKey " + elasticKey);
            request.setEntity(new StringEntity(queryJson));

            LogUtil.info("RAG SEARCH START:" + vectorIndex + ":"+ queryJson);

            org.apache.http.HttpResponse response = httpClient.execute(request);

            org.apache.http.HttpEntity entity = response.getEntity();

            LogUtil.info("RAG SEARCH END");

            if (entity != null) {
                String responseString = EntityUtils.toString(entity);
                ElasticResponse elasticResponse = new ElasticResponse(responseString);
                elasticResponse.initializeVectors();
                LogUtil.info("RAG SEARCH RESULT : " +
                        responseString!=null?responseString.substring(0,responseString.length()>1000?1000:responseString.length()):"" );

//                ragResult =  extractText(responseString);
                ragResult = elasticResponse.extractAllTexts();

                monitorLogService.ChatMonitorLog(chatroomDetail.getRoomId(), chatroomDetail.getSeq(), "RAG RESULT", ObjectMapperUtil.writeValueAsString(elasticResponse));
                if(ragResult == null || ragResult.isEmpty())
                    LogUtil.info("RAG SEARCH RESULT IS INVALID!!!");
            } else
                LogUtil.info("RAG SEARCH CALL IS FAIL!!!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ragResult;
    }

    public List<String> extractText(String jsonString) {
        // JSON 문자열이 포함된 JSONObject
        JSONObject jsonObject = new JSONObject(jsonString);

        // "hits" 배열 내의 객체에서 "_source" 객체의 "text" 필드를 추출
        JSONArray hitsArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
        List<String> texts = IntStream.range(0, hitsArray.length())
                .mapToObj(index -> hitsArray.getJSONObject(index)
                        .getJSONObject("_source")
                        .getString("text"))
                .collect(Collectors.toList());

        for(int i = 0 ; i < texts.size() ; i++) {
            LogUtil.info(texts.get(i));
        }

        return texts;

        // 결과 출력
//        texts.forEach(item -> { LogUtil.debug(item);});
    }


    private Flux<String> sendRagChat(ChatbotVO chatbot, ChatroomDetailVO chatroomDetailVO, String roomId, List<OpenAIChatMessage> messages) throws Exception {
        EngineEntity ragEngine = engineRepository.getReferenceById(chatbot.getRetrieverEngineId());
        LogUtil.debug(ragEngine.getName());
        StringBuffer sbContents = new StringBuffer();
        Long ragEngineId = Long.valueOf(ragEngine.getId());

        //파라미터 구성
        //일단 MAUMAI-풀무원 RAG에 대해서만 처리 - 향후 모델 추가시 모델에 따른 예외 처리는 어떻게 할지... 는 나중에 고민하는 걸로..
        MaumAIRequest payload = setPayLoadMaumAI(chatbot, ragEngineId, messages);

        LogUtil.info("!!!PAYLOAD:" + new Gson().toJson(payload));

        String endpoint = ragEngine.getEndpoint();
        String strApik = ragEngine.getApik();

        String jsonPayload = "";
        String jsonParam = "";

        try {
            jsonPayload = ObjectMapperUtil.writeValueAsString(payload);
            jsonParam =  ObjectMapperUtil.writeValueAsString(payload.getParam());
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }

        LogUtil.debug("befendpoint:" + endpoint);
        LogUtil.info("jsonPayload:" + jsonPayload);
        LogUtil.debug("jsonParam:" + jsonParam);

        sendSocketMessage(roomId, "mxCell", jsonParam);

        // Call Start - 함수화 검토 필요.
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            HttpRequest request = requestBuilder.build();

            // 동기 호출을 위한 send, 비동기는 sendAsync를 사용
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            try (Scanner scanner = new Scanner(response.body())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    LogUtil.debug("response:" + line);
                    sbContents.append(line);
                }
            }

            // 여기서는 단순히 응답 본문을 문자열로 반환
            // 실제 사용시에는 응답 코드에 따른 추가 처리가 필요할 수 있습니다.
            LogUtil.info("resonse:" + sbContents.toString());

            MaumAIRagResponse ragResponse = ObjectMapperUtil.readValue(sbContents.toString(), MaumAIRagResponse.class);
            sendSocketMessage(roomId, payload.getName(), ObjectMapperUtil.writeValueAsString(ragResponse.getKnowledgeItems()));

            List<MaumAIRagKnowlegeItem> knowlegeItems = ragResponse.getKnowledgeItems();

            String informJsonString = "";

            if(knowlegeItems != null && knowlegeItems.size() > 0){
                informJsonString = new Gson().toJson(knowlegeItems);
            }

            //기존 Prompt에 rag 데이터 얹기...
            String llmPrompt = chatbot.getPromptRequirement() + "\n" +
                    "정보는 [{\"score\":0.98, \"text\":\"정보1\"}, {\"score\":0.98, \"text\":\"정보1\"}] 형태의 json list로 구성 되어 있어\n" +
                    "점수가 높을수록 신뢰도가 있고 답변은 text의 내용을 기반으로 하면 되.";

            if(informJsonString != null && !informJsonString.isEmpty())
                llmPrompt += "\n#정보#\n" + informJsonString;

            LogUtil.info("llmPrompt:" + llmPrompt);
            LogUtil.debug("informJsonString:" + informJsonString);
            chatbot.setPromptRequirement(llmPrompt);

        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생시 적절히 처리
            LogUtil.error("ERROR1:" + sbContents.toString());
            return Flux.just("ERROR:" + sbContents.toString());
        }

        //마지막 LLM 호출은 chatbotType을 LLM으로 변경해서 호출 한다.
        //chatbot.setChatbotTypeCd("LLM");
        return sendLlmChat(chatbot, chatroomDetailVO, roomId, messages, null, null);
    }

    public String processJson(String jsonText) {

        StringBuffer sbContents = new StringBuffer();

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonText, JsonObject.class);
            // JSON 객체에서 필요한 데이터를 추출하고 처리
            // 예: jsonObject.get("id").getAsString()

            OpenAIResponse res = gson.fromJson(jsonText, OpenAIResponse.class);
            List<OpenAIResponse.Choice> choices = res.getChoices();

            if (choices != null) {
                for (OpenAIResponse.Choice choice : choices) {
                    if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                        sbContents.append(choice.getDelta().getContent());
                    } else if (choice.getMessages() != null) { //버전에 따라서 choice 내부에 message로 conatents가  내려오는 경우도 있음.
                        List<OpenAIResponse.Message> messages = choice.getMessages();
                        for (OpenAIResponse.Message message : messages) {
                            if (message.getDelta() != null && message.getDelta().getContent() != null) {
                                sbContents.append(message.getDelta().getContent());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }

        return sbContents.toString();
    }

    @Deprecated
    public CompletableFuture<ResponseEntity<String>> _getOpenAICompletion(String token, OpenAIRequest payload) {
        String url = "https://api.openai.com/v1/chat/completions"; // OpenAI API 엔드포인트

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token); // Bearer 토큰으로 인증

        // HTTP 요청 본문 구성
        HttpEntity<OpenAIRequest> entity = new HttpEntity<>(payload, headers);

        restTemplate.getInterceptors().add(new RequestResponseLoggingInterceptor());


        // 비동기로 OpenAI API 엔드포인트에 POST 요청
        return CompletableFuture.supplyAsync(() -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    @Deprecated
    public void printOpenAICompletionResult(String token, OpenAIRequest payload) {
        CompletableFuture<ResponseEntity<String>> future = _getOpenAICompletion(token, payload);

        future.thenAccept(responseEntity -> {
            System.out.println("Response from OpenAI: ");
            System.out.println(responseEntity.getBody());
        }).exceptionally(exception -> {
            System.err.println("Error calling OpenAI API: " + exception.getMessage());
            return null;
        });
    }

    @Deprecated
    // Azure 제공 라이브러리 사용할 경우 아래 코드 사용
    public Flux<String> getAzureOpenAI(OpenAIRequest payload, String roomId, EngineEntity llmEngine) throws Exception {
        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(llmEngine.getApik()))
                .endpoint(llmEngine.getEndpoint())
                .serviceVersion(OpenAIServiceVersion.valueOf(llmEngine.getVersion()))
                .buildAsyncClient();
        String model = llmEngine.getModel();

        List<ChatRequestMessage> chatMessages = new ArrayList<>();

        List<OpenAIChatMessage> messages = payload.getMessages();
        messages.forEach(message -> {
            switch (message.getRole()) {
                case "system":
                    chatMessages.add(new ChatRequestSystemMessage(message.getContent()));
                    break;
                case "user":
                    chatMessages.add(new ChatRequestUserMessage(message.getContent()));
                    break;
                case "assistant":
                    chatMessages.add(new ChatRequestAssistantMessage(message.getContent()));
                    break;
            }
        });

        ChatCompletionsOptions chatCompletionsOptions=  new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setStream(true);

        chatCompletionsOptions.setTopP(payload.getTopP());
        chatCompletionsOptions.setTemperature(payload.getTemperature());
        chatCompletionsOptions.setPresencePenalty(payload.getPresencePenalty());
        chatCompletionsOptions.setFrequencyPenalty(payload.getFrequencyPenalty());

        LogUtil.info("top_p:" + chatCompletionsOptions.getTopP());
        LogUtil.info("temperature:" + chatCompletionsOptions.getTemperature());
        LogUtil.info("presence_penalty:" + chatCompletionsOptions.getPresencePenalty());
        LogUtil.info("frequncy_penalty:" + chatCompletionsOptions.getFrequencyPenalty());

        return Flux.create(sink -> {
            client.getChatCompletionsStream(model, chatCompletionsOptions)
                    .subscribe(chatCompletion -> {
                        String modifiedData = processResponseAzureWithListAsString(chatCompletion);
                        sink.next(modifiedData); // 스트림에 데이터 추가
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {

                        }
                    }, error -> sink.error(error), () -> sink.complete()); // 오류 처리 및 스트림 완료
        });

    }

    @Deprecated
    private Flux<String> processResponseAzure(ChatCompletions chatCompletions) {
        StringBuffer sbContents = new StringBuffer();
        List<ChatChoice> choices = chatCompletions.getChoices();

        if (choices != null) {
            for (ChatChoice choice : choices) {
                if (choice.getDelta() != null) {
                    sbContents.append(choice.getDelta().getContent());
                }
            }
        } else {
            return Flux.just("");
        }

        if(sbContents.toString().equals("null"))
            return Flux.just("");
        log.debug("content1[{}]", sbContents.toString());
        return Flux.just(sbContents.toString());
    }

    @Deprecated
    private Flux<String> processResponseAzureWithList(List<ChatCompletions> chatCompletions) {
        StringBuffer sbContents = new StringBuffer();

        for(ChatCompletions chat:chatCompletions) {
            List<ChatChoice> choices = chat.getChoices();

            if (choices != null) {
                for (ChatChoice choice : choices) {
                    if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                        String strContent = choice.getDelta().getContent().toString();
                        if (strContent != null && !strContent.isEmpty() && !strContent.equals("null")) {
                            sbContents.append(strContent);
                            LogUtil.debug("strContent:" + strContent);
                        }
                    }
                }
            }

        }
        return Flux.just(sbContents.toString());
    }
}
