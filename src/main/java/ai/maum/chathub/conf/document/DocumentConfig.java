package ai.maum.chathub.conf.document;

import ai.maum.chathub.meta.SecurityMeta;
import ai.maum.chathub.util.StringUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger 설정
 * @author baekgol
 */
@Configuration
public class DocumentConfig {
    @Value("${service.doc.title}")
    private String title;

    @Value("${service.doc.desc}")
    private String desc;

    @Value("${service.doc.security.status}")
    private boolean securityStatus;

    @Value("${service.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI documentInfo() {
        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(desc))
                .servers(List.of(new Server().url(serverUrl)));

        if(securityStatus) openApi.schemaRequirement("JWT 토큰", new SecurityScheme()
                        .name("Authorization")
                        .in(SecurityScheme.In.HEADER)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("인증 및 인가된 사용자 검증 시 필요하다."));

        return openApi;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public OpenApiCustomiser documentCustomiser() {
        return openApi -> {
            List<SecurityRequirement> securityReqs = List.of(new SecurityRequirement().addList("JWT 토큰"));

            openApi.getPaths()
                    .forEach((pathName, pathInfo) -> {
                        List<Operation> operations = new ArrayList<>();

                        if(pathInfo.getGet() != null) operations.add(pathInfo.getGet());
                        if(pathInfo.getPost() != null) operations.add(pathInfo.getPost());
                        if(pathInfo.getPut() != null) operations.add(pathInfo.getPut());
                        if(pathInfo.getPatch() != null) operations.add(pathInfo.getPatch());
                        if(pathInfo.getDelete() != null) operations.add(pathInfo.getDelete());

                        for(Operation operation: operations) {
                            List<SecurityRequirement> srs = operation.getSecurity();

                            if(srs == null || srs.stream().noneMatch(sr -> sr.containsKey(SecurityMeta.DOCUMENT_NO_TOKEN)))
                                operation.setSecurity(securityReqs);
                            else operation.setSecurity(null);
                        }
                    });

            openApi.getComponents().getSchemas()
                    .forEach((schName, schInfo) -> {
                        Schema<?> schema = (Schema<?>)schInfo;
                        Map<String, Schema> newPp = new LinkedHashMap<>();

                        if(schema.getProperties() != null)
                            schema.getProperties().forEach((ppName, ppInfo) -> {
                                String name = StringUtil.convertNaming(ppName, true);
                                Schema<?> property = (Schema<?>)ppInfo;
                                property.setName(name);

                                String ref = property.get$ref();
                                Class<?> cs = property.getClass();

                                if(ref != null
                                        && (ref.equals("#/components/schemas/ObjectId")
                                        || ref.equals("#/components/schemas/LocalTime"))) {
                                    property.set$ref(null);
                                    property.setType("string");
                                    if(ref.equals("#/components/schemas/ObjectId")) property.setExample("507f1f77bcf86cd799439011");
                                    else property.setExample("12:00:00");
                                }
                                else if(cs == DateSchema.class || cs == DateTimeSchema.class) {
                                    StringSchema ss = new StringSchema();
                                    ss.setTitle(property.getTitle());
                                    ss.setDescription(property.getDescription());

                                    if(cs == DateSchema.class) ss.setExample("2023-05-31");
                                    else ss.setExample("2023-05-31 12:00:00");

                                    property = ss;
                                }

                                newPp.put(name, property);
                            });

                        schema.setProperties(newPp);
                    });
        };
    }
}
