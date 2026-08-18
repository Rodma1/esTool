package com.chen.service.ai;

import com.chen.model.ai.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LLM HTTP 客户端，兼容 OpenAI API 格式
 */
@Slf4j
@Component
public class LLMClient {

    @Value("${ai.llm.api-key:}")
    private String apiKey;

    @Value("${ai.llm.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.llm.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.llm.timeout:60}")
    private int timeoutSeconds;

    @Value("${ai.llm.max-tokens:4096}")
    private int maxTokens;

    private HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * 发送对话请求
     *
     * @param messages 消息列表
     * @return AI 回复内容
     */
    public String chatCompletion(List<ChatMessage> messages) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("AI API Key 未配置，请检查 application.yml 中的 ai.llm.api-key");
            throw new RuntimeException("AI API Key 未配置");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            // max_tokens 可能导致某些 provider pool 无法匹配，先不传
            if (maxTokens > 0) {
                requestBody.put("max_tokens", maxTokens);
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "ES-Tool/1.0")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            log.debug("LLM 请求: model={}, messages={}", model, messages.size());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                log.error("LLM 请求失败: status={}, body={}", response.statusCode(), responseBody);
                throw new RuntimeException("LLM 请求失败 [" + response.statusCode() + "]: " + responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                log.debug("LLM 回复: {}", content);
                return content;
            }

            throw new RuntimeException("LLM 响应格式异常，未找到 choices");
        } catch (Exception e) {
            log.error("LLM 调用异常", e);
            throw new RuntimeException("LLM 调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 流式对话请求
     */
    public String chatCompletionStream(List<ChatMessage> messages, Consumer<String> tokenConsumer) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("AI API Key 未配置");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            if (maxTokens > 0) {
                requestBody.put("max_tokens", maxTokens);
            }

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("User-Agent", "ES-Tool/1.0")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<Stream<String>> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() != 200) {
                String errorBody = response.body().collect(Collectors.joining());
                log.error("LLM 流式请求失败: status={}, body={}", response.statusCode(), errorBody);
                throw new RuntimeException("LLM 请求失败 [" + response.statusCode() + "]");
            }

            StringBuilder fullReply = new StringBuilder();
            response.body().forEach(line -> {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) {
                        return;
                    }
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode delta = node.path("choices").get(0).path("delta").path("content");
                        if (!delta.isMissingNode() && !delta.isNull()) {
                            String token = delta.asText();
                            fullReply.append(token);
                            tokenConsumer.accept(token);
                        }
                    } catch (Exception e) {
                        log.warn("解析 SSE 行失败: {}", line, e);
                    }
                }
            });

            return fullReply.toString();
        } catch (Exception e) {
            log.error("LLM 流式调用异常", e);
            throw new RuntimeException("LLM 流式调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 单轮快速对话
     */
    public String quickChat(String systemPrompt, String userPrompt) {
        return chatCompletion(List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(userPrompt)
        ));
    }
}
