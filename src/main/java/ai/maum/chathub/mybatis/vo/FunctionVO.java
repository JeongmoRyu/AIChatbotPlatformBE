package ai.maum.chathub.mybatis.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FunctionVO {
    private Long id;
    private Long userKey;
    private Long chatbotId;
    private String name;
    private String description;
    private String imgPath;
    private String filterPrefix;
    private List<Integer> preInfoType;
    private String useYn;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Object> fileList;
    private String questionName;
    private String questionDetail;
    private String questionImage;
    private Boolean isMine;

    @JsonIgnore
    private String fileListString;
    @JsonIgnore
    private String preInfoTypeString;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void setFileListString(String fileListString) {
        try {
            this.fileList = objectMapper.readValue(fileListString, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            this.fileList = new ArrayList<>(); // 에러가 발생하면 빈 리스트로 설정
        }
        this.fileListString = fileListString;
    }
    public void setPreInfoTypeString(String preInfoTypeString) {
        try {
            String jsonFormattedString = preInfoTypeString.replace("{", "[").replace("}", "]");
            this.preInfoType = objectMapper.readValue(jsonFormattedString, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            this.preInfoType = new ArrayList<>(); // 에러가 발생하면 빈 리스트로 설정
        }
        this.preInfoTypeString = preInfoTypeString;
    }

    public String getPreInfoTypeString() {
        if(this.preInfoTypeString == null || this.preInfoTypeString.isBlank() )
            try {
                this.preInfoTypeString = objectMapper.writeValueAsString(this.preInfoType);
            } catch (Exception e) {
                this.preInfoTypeString = "[]";
            }

        return this.preInfoTypeString;
    }
}
