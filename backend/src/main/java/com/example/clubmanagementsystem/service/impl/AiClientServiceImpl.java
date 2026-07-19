package com.example.clubmanagementsystem.service.impl;

import com.example.clubmanagementsystem.config.AiConfig;
import com.example.clubmanagementsystem.service.AiClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class AiClientServiceImpl implements AiClientService {

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AiClientServiceImpl(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
    }

    @Override
    public String chat(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", aiConfig.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是一位高校社团运营分析专家，请根据提供的数据给出专业的运营评估与建议。"),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7
            );

            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("choices").path(0).path("message").path("content").asText();
            } else {
                return "AI 服务调用失败，状态码：" + response.statusCode();
            }
        } catch (Exception e) {
            return "AI 服务暂时不可用：" + e.getMessage();
        }
    }

    @Override
    public String chatStream(List<Map<String, String>> messages, Consumer<String> onToken) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", true);

            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

            StringBuilder fullText = new StringBuilder();

            response.body().forEach(line -> {
                if (line.isEmpty() || !line.startsWith("data: ")) return;
                String data = line.substring(6).trim();
                if ("[DONE]".equals(data)) return;

                try {
                    JsonNode root = objectMapper.readTree(data);
                    String content = root.path("choices").path(0).path("delta").path("content").asText("");
                    if (!content.isEmpty()) {
                        fullText.append(content);
                        onToken.accept(content);
                    }
                } catch (Exception ignored) {
                }
            });

            return fullText.toString();
        } catch (Exception e) {
            throw new RuntimeException("流式调用失败：" + e.getMessage(), e);
        }
    }
}
