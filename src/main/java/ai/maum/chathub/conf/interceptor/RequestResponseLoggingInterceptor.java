package ai.maum.chathub.conf.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        if (body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            // 여기에서 요청 본문 로깅
            System.out.println("Request Body: " + requestBody);
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        // 응답 본문 로깅을 위한 로직, 비슷하게 구현
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        System.out.println("Response Body: " + responseBody);
    }
}
