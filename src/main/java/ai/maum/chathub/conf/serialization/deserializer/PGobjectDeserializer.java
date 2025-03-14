package ai.maum.chathub.conf.serialization.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.SQLException;

public class PGobjectDeserializer  extends JsonDeserializer<PGobject> {
    @Override
    public PGobject deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        PGobject pgObject = new PGobject();
        try {
            pgObject.setType(node.get("type").asText());
            pgObject.setValue(node.get("value").asText());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return pgObject;
    }
}
