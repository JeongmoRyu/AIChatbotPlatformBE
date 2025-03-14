package ai.maum.chathub.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class WebClientUtil {

    public static String callUrl(HttpMethod method, String urlString) {
        try {
            URI uri = new URI(urlString);
            String baseUrl = uri.getScheme() + "://" + uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();

            WebClient webClient = WebClient.create(baseUrl);

            Mono<String> response = webClient.method(method)
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .query(query)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class);

            return response.block();
        } catch (URISyntaxException e) {
            log.error("Invalid URL: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        String method = "GET";
        String urlString = "https://www.example.com/path/to/resource?param1=value1&param2=value2";

        String result = callUrl(HttpMethod.POST, urlString);
        System.out.println("Result: " + result);
    }
}
