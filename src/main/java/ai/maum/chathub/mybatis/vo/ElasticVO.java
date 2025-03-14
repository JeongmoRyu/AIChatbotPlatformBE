package ai.maum.chathub.mybatis.vo;

import ai.maum.chathub.api.engine.dto.EngineParam;
import ai.maum.chathub.api.engine.dto.EngineParamsConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class ElasticVO {
    @Schema(description = "엔진ID", example = "1")
    private Long  id;

    @Schema(description = "url", example = "https://xxxx.openai.azure.com")
    private String url;

    @Schema(description = "엔진구분키", example = "sk-xxxxxxxxx")
    private String apik;

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
    private List<EngineParam> parameters;

    @Schema(description = "엔진명", example = "gpt-4-turbo-preview")
    private String name;

    @Schema(description = "등록일", example = "2024-01-24 14:10:01")
    private Timestamp createdAt;

    @Schema(description = "수정일", example = "2024-01-24 14:10:02")
    private Timestamp updatedAt;

    @Schema(description = "인덱스", example = "aabc-mcl-wiki/_search")
    private String index1;

    @Schema(description = "인덱스(백업)", example = "aabc-mcl-wiki-backup/_search")
    private String index2;

    public void setParameters(String parameters) {
        EngineParamsConverter converter = new EngineParamsConverter();
        this.parameters = converter.convertToEntityAttribute(parameters);
    }


}
