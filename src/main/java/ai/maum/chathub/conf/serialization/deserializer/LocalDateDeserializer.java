package ai.maum.chathub.conf.serialization.deserializer;

import ai.maum.chathub.meta.RegexMeta;
import ai.maum.chathub.util.StringUtil;
import ai.maum.chathub.meta.DateMeta;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 형식의 문자열을 LocalDate로 역직렬화하는 Deserializer
 * @author baekgol
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.DATE_FORMAT);

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();

        if(StringUtil.matches(text, RegexMeta.MULTIPART_DATE_FORMAT)) {
            String[] info = text.replaceAll("[\\[\\]]", "").split(",");
            text = info[0]
                    + "-"
                    + String.format("%02d", Integer.parseInt(info[1]))
                    + "-"
                    + String.format("%02d", Integer.parseInt(info[2]));
        }

        return LocalDate.parse(text, formatter);
    }
}
