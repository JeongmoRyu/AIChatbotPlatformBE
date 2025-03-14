package ai.maum.chathub.conf.serialization.deserializer;

import ai.maum.chathub.meta.DateMeta;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 시간 형식의 문자열을 LocalTime으로 역직렬화하는 Deserializer
 * @author baekgol
 */
public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.TIME_FORMAT);

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return LocalTime.parse(p.getText(), formatter);
    }
}
