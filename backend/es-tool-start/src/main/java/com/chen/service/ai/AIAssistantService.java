package com.chen.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchHttpRequestParam;
import com.chen.model.ai.*;
import com.chen.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AI 助手业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantService {

    private final LLMClient llmClient;
    private final IndexCatalogService indexCatalogService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ElasticsearchService elasticsearchService;
    private final AIHistoryService historyService;

    /**
     * 通用 AI 对话（问答模式）
     */
    public AIChatResponse chat(AIChatRequest request) {
        try {
            List<ChatMessage> messages = buildMessages(request, PromptTemplate.qaSystemPrompt());
            String reply = llmClient.chatCompletion(messages);
            return AIChatResponse.success(reply);
        } catch (Exception e) {
            log.error("AI 对话失败", e);
            return AIChatResponse.error("AI 对话失败: " + e.getMessage());
        }
    }

    /**
     * 流式 AI 对话（问答模式）
     */
    public void chatStream(AIChatRequest request, Consumer<String> tokenConsumer) {
        try {
            List<ChatMessage> messages = buildMessages(request, PromptTemplate.qaSystemPrompt());
            llmClient.chatCompletionStream(messages, tokenConsumer);
        } catch (Exception e) {
            log.error("AI 流式对话失败", e);
            tokenConsumer.accept("\n\n[错误] AI 对话失败: " + e.getMessage());
        }
    }

    /**
     * 生成 ES DSL（不执行）
     */
    public AIChatResponse generateQuery(AIChatRequest request) {
        try {
            String dslJson = callLLMForDsl(request);
            return parseDslResponse(dslJson);
        } catch (Exception e) {
            log.error("生成查询失败", e);
            return AIChatResponse.error("生成查询失败: " + e.getMessage());
        }
    }

    /**
     * 生成 DSL 并执行查询
     */
    public AIChatResponse executeQuery(AIChatRequest request) {
        ElasticsearchConnectParam connectParam = request.getConnectParam();
        try {
            // 1. 生成 DSL
            String dslJson = callLLMForDsl(request);
            AIChatResponse parsed = parseDslResponse(dslJson);
            if (!parsed.isSuccess()) {
                return parsed;
            }

            // 2. 安全校验：拒绝写入操作
            String dslStr = parsed.getDsl();
            if (!isSafeDsl(dslStr)) {
                return AIChatResponse.error("检测到非查询操作，已拒绝执行。AI 助手目前仅支持 search / count / aggregate 查询。");
            }

            // 3. 执行查询
            if (connectParam == null || connectParam.getPort() == null) {
                return AIChatResponse.error("未配置 ES 连接，请先连接 Elasticsearch");
            }

            ElasticsearchHttpRequestParam httpParam = new ElasticsearchHttpRequestParam();
            httpParam.setHostName(connectParam.getHostName());
            httpParam.setPort(connectParam.getPort());
            httpParam.setScheme(connectParam.getScheme());
            httpParam.setUserName(connectParam.getUserName());
            httpParam.setPassword(connectParam.getPassword());
            httpParam.setMethod("POST");
            httpParam.setEndpoint("/" + parsed.getIndexPattern() + "/_search");
            httpParam.setBody(dslStr);

            Object result = elasticsearchService.httpOperation(httpParam);
            parsed.setExecutionResult(result);

            // 4. 保存成功历史（异步，不阻塞返回）
            try {
                historyService.saveHistory(connectParam, request.getUserInput(), dslStr,
                        parsed.getIndexPattern(), parsed.getExplanation(), true, null);
            } catch (Exception ex) {
                log.warn("保存查询历史失败（不影响主流程）", ex);
            }

            // 5. 可选：结果总结（简化版，避免过多 token）
            String resultStr = JSON.toJSONString(result);
            if (resultStr.length() < 5000) {
                try {
                    String summary = llmClient.quickChat(
                            PromptTemplate.resultSummaryPrompt(),
                            "查询结果：\n" + resultStr
                    );
                    parsed.setReply(summary);
                } catch (Exception e) {
                    log.warn("结果总结失败，跳过: {}", e.getMessage());
                    parsed.setReply("查询已执行，共返回结果。详情见下方 JSON。");
                }
            } else {
                parsed.setReply("查询已执行，结果数据量较大，详情见下方 JSON。");
            }

            return parsed;
        } catch (Exception e) {
            log.error("执行查询失败", e);
            // 保存失败历史
            if (connectParam != null && connectParam.getPort() != null) {
                try {
                    historyService.saveHistory(connectParam, request.getUserInput(), "",
                            "", "", false, null);
                } catch (Exception ex) {
                    log.warn("保存失败历史失败", ex);
                }
            }
            return AIChatResponse.error("执行查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取索引目录
     */
    public List<IndexMeta> getIndexCatalog(ElasticsearchConnectParam param) {
        return indexCatalogService.getIndexCatalog(param);
    }

    /**
     * 刷新索引目录
     */
    public List<IndexMeta> refreshIndexCatalog(ElasticsearchConnectParam param) {
        return indexCatalogService.refreshIndexCatalog(param);
    }

    /**
     * AI 生成知识库草稿
     */
    public List<KnowledgeBaseItem> generateKnowledgeBaseDraft(ElasticsearchConnectParam param) {
        List<IndexMeta> indices = indexCatalogService.getIndexCatalog(param);
        if (indices.isEmpty()) {
            indices = indexCatalogService.refreshIndexCatalog(param);
        }

        // 限制索引数量，避免 prompt 过大导致 LLM 输出截断或超时
        int limit = Math.min(indices.size(), 10);
        List<IndexMeta> limitedIndices = indices.subList(0, limit);

        // 构建提示词内容
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请为以下 ").append(limitedIndices.size()).append(" 个索引生成知识库配置：\n\n");
        for (IndexMeta meta : limitedIndices) {
            userPrompt.append("索引名: ").append(meta.getName()).append("\n");
            if (meta.getFieldSummary() != null && !meta.getFieldSummary().isEmpty()) {
                userPrompt.append("字段结构:\n");
                meta.getFieldSummary().forEach((field, type) ->
                        userPrompt.append("  - ").append(field).append(": ").append(type).append("\n")
                );
            }
            userPrompt.append("\n");
        }

        log.info("正在请求 AI 生成知识库草稿，索引数: {}", limitedIndices.size());
        String response = llmClient.quickChat(PromptTemplate.kbDraftSystemPrompt(), userPrompt.toString());
        log.info("AI 知识库草稿返回长度: {} 字符", response != null ? response.length() : 0);

        // 解析 JSON 数组
        return parseKnowledgeBaseResponse(response);
    }

    /**
     * 解析知识库响应，增加多层容错
     */
    private List<KnowledgeBaseItem> parseKnowledgeBaseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("AI 返回内容为空");
        }

        // 第一层：尝试直接提取并解析 JSON 数组
        String cleanJson = extractJsonFromMarkdown(response);
        try {
            List<KnowledgeBaseItem> result = JSON.parseArray(cleanJson, KnowledgeBaseItem.class);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            log.warn("第一层 JSON 数组解析失败，尝试容错解析");
        }

        // 第二层：尝试解析为单个对象并包装成数组
        try {
            KnowledgeBaseItem single = JSON.parseObject(cleanJson, KnowledgeBaseItem.class);
            if (single != null) {
                log.info("AI 返回的是单个对象，自动包装为数组");
                return List.of(single);
            }
        } catch (Exception e) {
            log.warn("第二层单对象解析失败");
        }

        // 第三层：尝试从文本中定位 JSON 数组（找到第一个 [ 和最后一个 ]）
        try {
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start >= 0 && end > start) {
                String extracted = response.substring(start, end + 1);
                List<KnowledgeBaseItem> result = JSON.parseArray(extracted, KnowledgeBaseItem.class);
                if (result != null && !result.isEmpty()) {
                    log.info("通过字符定位成功提取 JSON 数组");
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("第三层字符定位解析失败");
        }

        // 第四层：尝试从文本中定位 JSON 对象（找到第一个 { 和最后一个 }）
        try {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String extracted = response.substring(start, end + 1);
                KnowledgeBaseItem single = JSON.parseObject(extracted, KnowledgeBaseItem.class);
                if (single != null) {
                    log.info("通过字符定位成功提取 JSON 对象");
                    return List.of(single);
                }
            }
        } catch (Exception e) {
            log.warn("第四层对象定位解析失败");
        }

        // 全部失败，打印原始响应用于排查
        log.error("解析知识库草稿失败，原始响应前500字符: {}",
                response.length() > 500 ? response.substring(0, 500) + "..." : response);
        throw new RuntimeException("AI 生成的知识库格式不正确，请重试或手动编辑。原始响应前200字："
                + (response.length() > 200 ? response.substring(0, 200) : response));
    }

    // ==================== 私有方法 ====================

    /**
     * 调用 LLM 生成 DSL
     */
    private String callLLMForDsl(AIChatRequest request) {
        ElasticsearchConnectParam connectParam = request.getConnectParam();
        String esVersion = connectParam != null ? connectParam.getVersion() : "";

        // 本地预筛选：获取相关索引和知识库
        List<IndexMeta> relevantIndices = new ArrayList<>();
        List<KnowledgeBaseItem> relevantKb = new ArrayList<>();
        List<Map<String, Object>> similarQueries = new ArrayList<>();

        if (connectParam != null && connectParam.getPort() != null) {
            relevantIndices = indexCatalogService.findRelevantIndices(request.getUserInput(), connectParam);
            relevantKb = knowledgeBaseService.findRelevant(request.getUserInput());
            try {
                similarQueries = historyService.searchSimilarQueries(connectParam, request.getUserInput(), 3);
            } catch (Exception e) {
                log.warn("搜索相似查询失败，跳过: {}", e.getMessage());
            }
        }

        String indexContext = buildIndexContext(relevantIndices);
        String kbContext = buildKbContext(relevantKb);
        String fewShotContext = buildFewShotContext(similarQueries);

        String systemPrompt = PromptTemplate.querySystemPrompt(esVersion, indexContext, kbContext);
        if (!fewShotContext.isEmpty()) {
            systemPrompt += "\n【历史成功案例参考】\n" + fewShotContext;
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messages.addAll(request.getMessages());
        }
        messages.add(ChatMessage.user(request.getUserInput()));

        return llmClient.chatCompletion(messages);
    }

    /**
     * 解析 DSL 响应
     */
    private AIChatResponse parseDslResponse(String dslJson) {
        try {
            String cleanJson = extractJsonFromMarkdown(dslJson);
            JSONObject obj = JSON.parseObject(cleanJson);
            AIChatResponse response = new AIChatResponse();
            response.setSuccess(true);
            response.setDsl(obj.getString("dsl"));
            response.setIndexPattern(obj.getString("indexPattern"));
            response.setExplanation(obj.getString("explanation"));
            response.setReply(obj.getString("explanation"));
            return response;
        } catch (Exception e) {
            log.error("解析 DSL 响应失败: {}", dslJson, e);
            return AIChatResponse.error("AI 返回格式不正确，无法解析 DSL。原始响应：\n" + dslJson);
        }
    }

    /**
     * 从 markdown 代码块中提取 JSON
     */
    private String extractJsonFromMarkdown(String text) {
        if (text == null) return "";
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    /**
     * 构建索引上下文字符串
     */
    private String buildIndexContext(List<IndexMeta> indices) {
        if (indices == null || indices.isEmpty()) {
            return "暂无可用索引上下文。";
        }
        StringBuilder sb = new StringBuilder();
        for (IndexMeta meta : indices) {
            sb.append("索引: ").append(meta.getName());
            if (meta.getAlias() != null && !meta.getAlias().isEmpty()) {
                sb.append(" (别名: ").append(meta.getAlias()).append(")");
            }
            sb.append("\n");
            if (meta.getFieldSummary() != null && !meta.getFieldSummary().isEmpty()) {
                sb.append("  字段: ");
                sb.append(meta.getFieldSummary().entrySet().stream()
                        .map(e -> e.getKey() + "(" + e.getValue() + ")")
                        .collect(Collectors.joining(", ")));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 构建知识库上下文字符串
     */
    private String buildKbContext(List<KnowledgeBaseItem> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (KnowledgeBaseItem item : items) {
            sb.append("业务: ").append(item.getBusinessName()).append("\n");
            sb.append("索引: ").append(String.join(", ", item.getIndices())).append("\n");
            if (item.getDescription() != null) {
                sb.append("描述: ").append(item.getDescription()).append("\n");
            }
            if (item.getFields() != null && !item.getFields().isEmpty()) {
                sb.append("字段说明:\n");
                item.getFields().forEach((name, meta) -> {
                    sb.append("  - ").append(name)
                            .append("(").append(meta.getType()).append(")");
                    if (meta.getDescription() != null) {
                        sb.append(": ").append(meta.getDescription());
                    }
                    sb.append("\n");
                });
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 构建 few-shot 上下文字符串
     */
    private String buildFewShotContext(List<Map<String, Object>> similarQueries) {
        if (similarQueries == null || similarQueries.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (Map<String, Object> item : similarQueries) {
            sb.append("示例").append(idx).append("：\n");
            sb.append("用户：").append(item.get("userQuery")).append("\n");
            sb.append("DSL：").append(item.get("dsl")).append("\n");
            if (item.get("explanation") != null) {
                sb.append("说明：").append(item.get("explanation")).append("\n");
            }
            sb.append("\n");
            idx++;
        }
        return sb.toString();
    }

    /**
     * 安全校验：只允许查询操作
     */
    private boolean isSafeDsl(String dsl) {
        if (dsl == null) return false;
        String lower = dsl.toLowerCase();
        // 拒绝明显的写入操作关键词
        String[] forbidden = {"delete", "update", "index", "bulk", "create", "put"};
        for (String word : forbidden) {
            // 简单字符串匹配，生产环境可用更严谨的 JSON 解析
            if (lower.contains("\"" + word + "\"")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 构造对话消息列表
     */
    private List<ChatMessage> buildMessages(AIChatRequest request, String systemPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messages.addAll(request.getMessages());
        }

        if (request.getUserInput() != null && !request.getUserInput().isEmpty()) {
            messages.add(ChatMessage.user(request.getUserInput()));
        }

        return messages;
    }
}
