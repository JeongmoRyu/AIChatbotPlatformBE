package ai.maum.chathub.conf.serialization.deserializer;

import ai.maum.chathub.meta.DateMeta;
import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.StringUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 및 시간 형식의 문자열을 LocalDateTime으로 역직렬화하는 Deserializer
 * @author baekgol
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_NORMAL);

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // [2023,5,31,12,1,23]
        String text = p.getText();

        if(StringUtil.matches(text, RegexMeta.MULTIPART_DATE_FORMAT)) {
            String[] info = text.replaceAll("[\\[\\]]", "").split(",");
            text = info[0]
                    + "-"
                    + String.format("%02d", Integer.parseInt(info[1]))
                    + "-"
                    + String.format("%02d", Integer.parseInt(info[2]))
                    + " "
                    + String.format("%02d", Integer.parseInt(info[3]))
                    + ":"
                    + String.format("%02d", Integer.parseInt(info[4]))
                    + ":"
                    + String.format("%02d", Integer.parseInt(info[5]));
        }

        return LocalDateTime.parse(text, formatter);
    }
}
