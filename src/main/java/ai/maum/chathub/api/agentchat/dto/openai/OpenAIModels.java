package ai.maum.chathub.api.agentchat.dto.openai;

public enum OpenAIModels {
    GPT_4("gpt-4", "GPT-4", 8192);
    // Define other models here

    private final String id;
    private final String name;
    private final int maxLimit;

    OpenAIModels(String id, String name, int maxLimit) {
        this.id = id;
        this.name = name;
        this.maxLimit = maxLimit;
    }
}
