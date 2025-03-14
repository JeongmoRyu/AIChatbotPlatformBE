package ai.maum.chathub.api.question.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import java.util.Collections;
import java.util.List;

public class QuestionListConverter implements AttributeConverter<List<Question>, String> {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Question> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting list of questions to JSON string", e);
        }
    }

    @Override
    public List<Question> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Question>>() {});
        } catch (Exception e) {
            // 로그 처리 및 빈 리스트 반환
            return Collections.emptyList();
        }
    }
}
