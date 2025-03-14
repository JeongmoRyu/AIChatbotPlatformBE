package ai.maum.chathub.api.question.service;

import ai.maum.chathub.api.chatbotInfo.entity.ChatbotInfoIdEntity;
import ai.maum.chathub.api.chatbotInfo.service.ChatbotInfoService;
import ai.maum.chathub.util.LogUtil;
import ai.maum.chathub.api.chat.dto.openai.OpenAIChatMessage;
import ai.maum.chathub.api.chat.dto.openai.OpenAIRequest;
import ai.maum.chathub.api.chatbot.entity.ChatbotEntity;
import ai.maum.chathub.api.chatbot.service.ChatbotService;
import ai.maum.chathub.api.chatroom.entity.ChatroomDetailEntity;
import ai.maum.chathub.api.chatroom.service.ChatroomService;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.engine.entity.EngineEntity;
import ai.maum.chathub.api.engine.repo.EngineRepository;
import ai.maum.chathub.api.question.dto.Question;
import ai.maum.chathub.api.question.entity.QuestionEntity;
import ai.maum.chathub.api.question.repo.QuestionRepository;
import ai.maum.chathub.api.chat.service.ChatService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionService {

    @Value("${service.question.question-generate-id}")
    private Long QUESTION_GENERATE_ID;

    private final EngineRepository engineRepository;
    private final QuestionRepository questionRepository;
    private final ChatService chatService;
    private final ChatbotService chatbotService;
    private final ChatroomService chatroomService;
    private final ChatbotInfoService chatbotInfoService;;

    public List<Question> suggestQuestion (Long chatbotId, Long chatroomId, Long seq) {

//        ChatbotEntity chatbot = chatbotService.getChatbotById(chatbotId);

        Map<String,Object> chatbot = chatbotInfoService.getGetChatbotInfoNormal(chatbotId);

        String name = "";
        String promptRole = "";
        String promptRequirement = "";

        if(chatbot == null) {
            ChatbotInfoIdEntity chatbotInfoIdEntity = chatbotInfoService.getGetbotInfoById(chatbotId);
            if(chatbotInfoIdEntity == null)
                throw BaseException.of("챗봇이 없습니다.");
            else {
                name = chatbotInfoIdEntity.getName();
                promptRole = chatbotInfoIdEntity.getDescription();
            }
        } else {
            if(chatbot.containsKey("name"))
                name = (String) chatbot.get("name");
            if(chatbot.containsKey("system_prompt"))
                promptRole = (String) chatbot.get("system_prompt");
            if(chatbot.containsKey("user_prompt"))
                promptRequirement = (String) chatbot.get("user_prompt");

//            name = chatbot.getName();
//            promptRole = chatbot.getPromptRole();
//            promptRequirement = chatbot.getPromptRequirement();
        }

        List<ChatroomDetailEntity> chatroomDetailList = chatroomService.getChatroomDetailByRoomIdAndSeq(chatroomId, seq);
        String userQ = null;
        String assatantA = null;


        if(chatroomDetailList != null || !chatroomDetailList.isEmpty()) {
            for(ChatroomDetailEntity item:chatroomDetailList) {
                String role = item.getRole()==null?null:item.getRole().toUpperCase();
                if("USER".equals(role))
                    userQ = item.getContent();
                else if("ASSISTANT".equals(role))
                    assatantA = item.getContent();
            }
        }

        return generateQuestion(name, promptRole, promptRequirement, userQ, assatantA);
    }
    public List<Question> generateQuestion (ChatbotEntity chatbot, String userQ, String assatantA) {
//        LogUtil.info("TITLE:" + chatbot.getName());
//        LogUtil.info("ROLE:" + chatbot.getPromptRole());
//        LogUtil.info("REQUIREMENT:" + chatbot.getPromptRequirement());

        String strQuestion = "제목:" + chatbot.getName() + "\n"
                + "역할:" + chatbot.getPromptRole() + "\n"
                + "지침:" + chatbot.getPromptRequirement();

        QuestionEntity question = null;

        if(userQ != null && assatantA != null) {
            strQuestion = "사용자의 질문:" + userQ + "\n"
                    + "챗봇의 대답:" + assatantA + "\n"
                    + strQuestion;
//            question = questionRepository.getReferenceById(2L);
//            question = questionRepository.getReferenceById(3L);
            question = questionRepository.getReferenceById(QUESTION_GENERATE_ID);
        } else {
            question = questionRepository.findFirstByUseYnOrderBySeq(true);
        }

        return generateQuestion(question, strQuestion);
    }


    public List<Question> generateQuestion (String name, String role, String requirement, String userQ, String assatantA) {
        ChatbotEntity chatbotEntity = new ChatbotEntity();
        chatbotEntity.setName(name);
        chatbotEntity.setPromptRole(role);
        chatbotEntity.setPromptRequirement(requirement);

        return generateQuestion(chatbotEntity, userQ, assatantA);
    }

    public List<Question> generateQuestion (ChatbotEntity chatbot) {
        return generateQuestion(chatbot, null, null);
//        LogUtil.info("TITLE:" + chatbot.getName());
//        LogUtil.info("ROLE:" + chatbot.getPromptRole());
//        LogUtil.info("REQUIREMENT:" + chatbot.getPromptRequirement());
//        String strQuestion = "제목:" + chatbot.getName() + "\n"
//                + "역할:" + chatbot.getPromptRole() + "\n"
//                + "지침:" + chatbot.getPromptRequirement();
//        QuestionEntity question = questionRepository.findFirstByUseYnOrderBySeq(true);
//        return generateQuestion(question, strQuestion);
    }

    private List<Question> generateQuestion (QuestionEntity question, String strQuestion)  {

//        QuestionEntity question = questionRepository.findFirstByUseYnOrderBySeq(true);

        List<Question> rtnQuestionList = new ArrayList<Question>();
        LogUtil.info("start of generateQuestion");


        OpenAIRequest payload = new OpenAIRequest();

        payload.setTopP(question.getTopP());
        payload.setTemperature(question.getTemperature());
        payload.setPresencePenalty(question.getPres_p());
        payload.setFrequencyPenalty(question.getFreq_p());
        payload.setMaxTokens(question.getMax_token());
        payload.setStream(true);

        List<OpenAIChatMessage> messages = new ArrayList<OpenAIChatMessage>();
        String strPrompt = question.getSystemPrompt();
        OpenAIChatMessage prompt = new OpenAIChatMessage("system", strPrompt);
        messages.add(prompt);
        OpenAIChatMessage questionMessage = new OpenAIChatMessage("user", strQuestion);
        messages.add(questionMessage);
        payload.setMessages(messages);

        EngineEntity engine = engineRepository.getReferenceById(question.getEngineId());

        try {

            LogUtil.debug("before1");
            LogUtil.info("before2");

//            Flux<String> flux = chatService.getLlmCompletion(payload, null, engine);

            String endpoint = engine.getEndpoint();
            String jsonPayload = "";
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            String strApik = engine.getApik();

            log.debug("befendpoint:" + endpoint);
            log.debug("strPayLoad:" + jsonPayload);

            try {

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest.Builder requestBuilder = null;

//                try {
//                    jsonPayload = objectMapper.writeValueAsString(payload);
//                } catch (Exception e) {
//                    LogUtil.error(e.getMessage());
//                }

                if(endpoint == null || endpoint.isBlank()) {
                    endpoint = "https://api.openai.com/v1/chat/completions";
                    payload.setModel(engine.getModel());
                    jsonPayload = objectMapper.writeValueAsString(payload);
                    strApik = "Bearer " + strApik;
                    requestBuilder = HttpRequest.newBuilder()
                            .uri(URI.create(endpoint))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header(HttpHeaders.AUTHORIZATION, strApik)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
                } else {
                    payload.setModel(engine.getModel());
                    requestBuilder = HttpRequest.newBuilder()
                            .uri(URI.create(endpoint))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("api-key", strApik)
                            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
                }

                HttpRequest request = requestBuilder.build();

                // 동기 호출을 위한 send, 비동기는 sendAsync를 사용
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                StringBuffer sbContents = new StringBuffer();

                try (Scanner scanner = new Scanner(response.body())) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("data: ")) {
                            String jsonText = line.substring(6); // "data: " 다음의 문자열을 JSON으로 간주
                            sbContents.append(chatService.processJson(jsonText));
                        }
                    }
                }

                // 여기서는 단순히 응답 본문을 문자열로 반환
                // 실제 사용시에는 응답 코드에 따른 추가 처리가 필요할 수 있습니다.
//                LogUtil.info("resonse:" + response.body());
                log.info("resonse:" + sbContents.toString());

                try {
                    Type listType = new TypeToken<List<Question>>(){}.getType();
                    rtnQuestionList = new Gson().fromJson(sbContents.toString(), listType);
                } catch (Exception e) {

                }

            } catch (Exception e) {
                e.printStackTrace();
                // 예외 발생시 적절히 처리
            }

//            try {
//                Flux<String> fluxResponse = chatService.getLlmCompletion(payload, null, engine);
//            } catch (Exception e) {
//            }

//            LogUtil.debug("after1");
//            LogUtil.info("after2");
        } catch (Exception e) {
//            LogUtil.debug("exception1");
//            LogUtil.info("exception2");
            e.printStackTrace();
        }

        return rtnQuestionList;
    }

    public List<Question> _generateQuestion (ChatbotEntity chatbot)  {

        QuestionEntity question = questionRepository.findFirstByUseYnOrderBySeq(true);

        List<Question> rtnQuestionList = new ArrayList<Question>();

        log.info("start of generateQuestion");

        log.info("TITLE:" + chatbot.getName());
        log.info("ROLE:" + chatbot.getPromptRole());
        log.info("REQUIREMENT:" + chatbot.getPromptRequirement());

        OpenAIRequest payload = new OpenAIRequest();

        payload.setTopP(question.getTopP());
        payload.setTemperature(question.getTemperature());
        payload.setPresencePenalty(question.getPres_p());
        payload.setFrequencyPenalty(question.getFreq_p());
        payload.setMaxTokens(question.getMax_token());
        payload.setStream(true);

        List<OpenAIChatMessage> messages = new ArrayList<OpenAIChatMessage>();
        String strPrompt = question.getSystemPrompt();
        OpenAIChatMessage prompt = new OpenAIChatMessage("system", strPrompt);
        messages.add(prompt);
        String strQuestion = "제목:" + chatbot.getName() + "\n"
                + "역할:" + chatbot.getPromptRole() + "\n"
                + "지침:" + chatbot.getPromptRequirement();
        OpenAIChatMessage questionMessage = new OpenAIChatMessage("user", strQuestion);
        messages.add(questionMessage);
        payload.setMessages(messages);

        EngineEntity engine = engineRepository.getReferenceById(question.getEngineId());

        try {

            log.debug("before1");
            log.info("before2");

//            Flux<String> flux = chatService.getLlmCompletion(payload, null, engine);

            String endpoint = engine.getEndpoint();
            String jsonPayload = "";
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            try {
                jsonPayload = objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            String strApik = engine.getApik();

            log.debug("befendpoint:" + endpoint);
            log.debug("strPayLoad:" + jsonPayload);

            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("api-key", strApik)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

                HttpRequest request = requestBuilder.build();

                // 동기 호출을 위한 send, 비동기는 sendAsync를 사용
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                StringBuffer sbContents = new StringBuffer();

                try (Scanner scanner = new Scanner(response.body())) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.startsWith("data: ")) {
                            String jsonText = line.substring(6); // "data: " 다음의 문자열을 JSON으로 간주
                            sbContents.append(chatService.processJson(jsonText));
                        }
                    }
                }

                // 여기서는 단순히 응답 본문을 문자열로 반환
                // 실제 사용시에는 응답 코드에 따른 추가 처리가 필요할 수 있습니다.
//                LogUtil.info("resonse:" + response.body());
                log.info("resonse:" + sbContents.toString());

                try {
                    Type listType = new TypeToken<List<Question>>(){}.getType();
                    rtnQuestionList = new Gson().fromJson(sbContents.toString(), listType);
                } catch (Exception e) {

                }

            } catch (Exception e) {
                e.printStackTrace();
                // 예외 발생시 적절히 처리
            }

//            try {
//                Flux<String> fluxResponse = chatService.getLlmCompletion(payload, null, engine);
//            } catch (Exception e) {
//            }

            log.debug("after1");
        } catch (Exception e) {
            log.debug("exception1");
            e.printStackTrace();
        }

        return rtnQuestionList;
    }
}
