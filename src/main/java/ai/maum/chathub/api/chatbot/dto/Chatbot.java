package ai.maum.chathub.api.chatbot.dto;

import ai.maum.chathub.api.chatbot.entity.ChatbotEntity;
import ai.maum.chathub.api.question.dto.Question;
import ai.maum.chathub.api.engine.dto.EngineParam;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Chatbot {
    private String userId;
    private long  id;
    private String name;
    private String chatbotTypeCd;
    private String promptRole;
    private String promptRequirement;
    private String promptTail;
    private Long retrieverEngineId;
    private Long llmEngineId;
    private Long tailEngineId;
    private List<EngineParam> ragParameters = new ArrayList<EngineParam>();
    private List<EngineParam> llmParameters = new ArrayList<EngineParam>();
    private List<EngineParam> tailParameters = new ArrayList<EngineParam>();
    private List<Question> questions = new ArrayList<Question>();
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Chatbot(ChatbotEntity chatbotEntity) {
        this.userId = String.valueOf(chatbotEntity.getUserId());
        this.id = Long.valueOf(chatbotEntity.getId());
        this.name = String.valueOf(chatbotEntity.getName());
        this.chatbotTypeCd = String.valueOf(chatbotEntity.getChatbotTypeCd());
        this.promptRole = String.valueOf(chatbotEntity.getPromptRole());
        this.promptRequirement = String.valueOf(chatbotEntity.getPromptRequirement());
        this.promptTail = String.valueOf(chatbotEntity.getPromptTail());
        this.retrieverEngineId = Long.valueOf(chatbotEntity.getRetrieverEngineId());
        this.llmEngineId = Long.valueOf(chatbotEntity.getLlmEngineId());
        this.tailEngineId = Long.valueOf(chatbotEntity.getTailEngineId());

        if(chatbotEntity.getRagParameters() != null)
            for(EngineParam param:chatbotEntity.getRagParameters())
                this.ragParameters.add(param);
        if(chatbotEntity.getLlmParameters() != null)
            for(EngineParam param:chatbotEntity.getLlmParameters())
                this.llmParameters.add(param);
        if(chatbotEntity.getTailParameters() != null)
            for(EngineParam param:chatbotEntity.getTailParameters())
                this.tailParameters.add(param);
        if(chatbotEntity.getQuestions()!= null)
            for(Question param:chatbotEntity.getQuestions())
                this.questions.add(param);
        this.createdAt = chatbotEntity.getCreatedAt();
        this.updatedAt = chatbotEntity.getUpdatedAt();
    }
}
