package ai.maum.chathub.api.engine.entity;

import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.dto.EngineParamsConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="llm_engine")
public class LlmEngineEntity {

    @Schema(description = "Retriever엔진ID", example = "1")
    private long retrieveEngineId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "LLM엔진ID", example = "1")
    private long id;
    @Schema(description = "VENDOR", example = "OPENAI")
    @Column(name = "vendor")
    private String vendor;
    @Schema(description = "LLM엔진명", example = "gpt-4-turbo-preview")
    private String name;
    @Schema(description = "순번", example = "1")
    private int seq;
    @Schema(description = "API KEY", example = "sk-xxxxxxxxx")
    private String apik;
    @Schema(description = "endpoint", example = "https://xxxx.openai.azure.com")
    private String endpoint;
    @Schema(description = "모델명", example = "gpt3-turbo")
    private String model;
    @Schema(description = "버전", example = "2023-06-01-preview")
    private String version;
    @Schema(description = "Top-K 기본값", example = "3")
    @Column(name = "top_k")
    private String topK;
    @Schema(description = "Top-P 기본값", example = "0.7")
    @Column(name = "top_p")
    private String topP;
    @Schema(description = "Presence Penalty 기본값", example = "0.7")
    @Column(name = "pres_p")
    private Float presP;
    @Schema(description = "Frequncy Penalty 기본값", example = "0.7")
    @Column(name = "freq_p")
    private Float freqP;

    @Schema(description = "파라미터(JSON)", example = "[\n" +
            "    {\n" +
            "        \"label\":\"Top K\",\n" +
            "        \"key\":\"top_k\", \n" +
            "        \"range\": {\n" +
            "          \"from\": \"1\",\n" +
            "          \"to\": \"10\"\n" +
            "        },\n" +
            "        \"mandatory\": true,\n" +
            "        \"value\":\"2\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"label\":\"Top P\",\n" +
            "        \"key\":\"top_p\", \n" +
            "        \"range\": {\n" +
            "          \"from\": \"0.00\",\n" +
            "          \"to\": \"1.00\"\n" +
            "        },\n" +
            "        \"mandntory\": true,\n" +
            "        \"value\":\"1\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"label\":\"Temperature\",\n" +
            "        \"key\":\"temp\", \n" +
            "        \"range\": {\n" +
            "          \"from\": \"0.00\",\n" +
            "          \"to\": \"2.00\"\n" +
            "        },\n" +
            "        \"mandatory\": true,\n" +
            "        \"value\":\"0.8\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"label\":\"Presense penalty\",\n" +
            "        \"key\":\"pres_p\", \n" +
            "        \"range\": {\n" +
            "          \"from\": \"0.00\",\n" +
            "          \"to\": \"2.00\"\n" +
            "        },\n" +
            "        \"mandatory\": true,\n" +
            "        \"value\":\"0.7\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"label\":\"Frequency penalty\",\n" +
            "        \"key\":\"freq_p\", \n" +
            "        \"range\": {\n" +
            "          \"from\": \"0.00\",\n" +
            "          \"to\": \"2.00\"\n" +
            "        },\n" +
            "        \"mandatory\": true,\n" +
            "        \"value\":\"0.5\"\n" +
            "    }\n" +
            "]")
    @Convert(converter = EngineParamsConverter.class)
    private List<EngineParam> parameters;

//    private String parameters;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;
}
