package ai.maum.chathub.api.engine.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class EngineParamsConverter implements AttributeConverter<List<EngineParam>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<EngineParam> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error converting List<EngineParam> to String", e);
        }
    }

    @Override
    public List<EngineParam> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<EngineParam>>(){});
        } catch (Exception e) {
            return new ArrayList<EngineParam>();
//            throw new RuntimeException("Error converting String to List<EngineParam>", e);
        }
    }
}



//package ai.maum.playground.api.engine.dto;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import javax.persistence.AttributeConverter;
//import javax.persistence.Converter;
//import java.util.List;
//
//@Converter
//public class EngineParamsConverter implements AttributeConverter<EngineParams, String> {
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public String convertToDatabaseColumn(EngineParams attribute) {
//        try {
//            return objectMapper.writeValueAsString(attribute.getParamaters());
//        } catch (Exception e) {
//            throw new RuntimeException("Error converting EngineParams to String", e);
//        }
//    }
//
//    @Override
//    public EngineParams convertToEntityAttribute(String dbData) {
//        try {
//            List<EngineParam> params = objectMapper.readValue(dbData, new TypeReference<List<EngineParam>>(){});
//            return new EngineParams(params);
//        } catch (Exception e) {
//            throw new RuntimeException("Error converting String to EngineParams", e);
//        }
//    }
//}
