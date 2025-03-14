package ai.maum.chathub.conf.serialization;

import ai.maum.chathub.conf.serialization.deserializer.LocalDateDeserializer;
import ai.maum.chathub.conf.serialization.serializer.LocalDateTimeSerializer;
import ai.maum.chathub.conf.serialization.serializer.LocalTimeSerializer;
import ai.maum.chathub.conf.serialization.serializer.LocalDateSerializer;
import ai.maum.chathub.conf.serialization.deserializer.LocalDateTimeDeserializer;
import ai.maum.chathub.conf.serialization.deserializer.LocalTimeDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 직렬화 및 역직렬화 설정
 * @author baekgol
 */
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.build()
                .registerModule(new SimpleModule()
                        .addSerializer(LocalTime.class, new LocalTimeSerializer())
                        .addSerializer(LocalDate.class, new LocalDateSerializer())
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer())
                        .addDeserializer(LocalTime.class, new LocalTimeDeserializer())
                        .addDeserializer(LocalDate.class, new LocalDateDeserializer())
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
    }
}
