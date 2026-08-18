package com.chen.controller.ai;

import com.chen.common.utils.resultreturn.ResultData;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.model.ai.AIChatRequest;
import com.chen.model.ai.AIChatResponse;
import com.chen.model.ai.IndexMeta;
import com.chen.model.ai.KnowledgeBaseItem;
import com.chen.service.ai.AIAssistantService;
import com.chen.service.ai.AIHistoryService;
import com.chen.service.ai.KnowledgeBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * AI 助手控制器
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@Api(value = "AI助手", tags = "AI智能助手接口")
@RequestMapping("/ai")
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AIHistoryService historyService;

    /**
     * AI 通用对话（问答模式）
     */
    @ApiOperation("AI通用对话")
    @PostMapping("/chat")
    public ResultData<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        AIChatResponse response = aiAssistantService.chat(request);
        return buildResult(response);
    }

    /**
     * AI 流式对话（SSE）
     */
    @ApiOperation("AI流式对话")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AIChatRequest request, HttpServletResponse response) {
        // 禁用 Undertow/Servlet 缓冲，确保 SSE 即时推送
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");

        SseEmitter emitter = new SseEmitter(600_000L);

        new Thread(() -> {
            try {
                aiAssistantService.chatStream(request, token -> {
                    try {
                        emitter.send(SseEmitter.event().data(token));
                    } catch (Exception e) {
                        log.warn("SSE 发送失败，客户端可能已断开", e);
                        emitter.completeWithError(e);
                    }
                });
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("流式对话异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        }).start();

        emitter.onTimeout(() -> log.warn("SSE 连接超时"));
        emitter.onError(ex -> log.warn("SSE 连接异常", ex));

        return emitter;
    }

    /**
     * 自然语言生成 ES DSL
     */
    @ApiOperation("AI生成查询DSL")
    @PostMapping("/generate-query")
    public ResultData<AIChatResponse> generateQuery(@RequestBody AIChatRequest request) {
        AIChatResponse response = aiAssistantService.generateQuery(request);
        return buildResult(response);
    }

    /**
     * 生成 DSL 并直接执行
     */
    @ApiOperation("AI生成并执行查询")
    @PostMapping("/execute-query")
    public ResultData<AIChatResponse> executeQuery(@RequestBody AIChatRequest request) {
        AIChatResponse response = aiAssistantService.executeQuery(request);
        return buildResult(response);
    }

    /**
     * 获取当前连接的索引目录
     */
    @ApiOperation("获取索引目录")
    @PostMapping("/indices")
    public ResultData<List<IndexMeta>> getIndices(@RequestBody ElasticsearchConnectParam param) {
        return ResultData.success(aiAssistantService.getIndexCatalog(param));
    }

    /**
     * 刷新索引目录
     */
    @ApiOperation("刷新索引目录")
    @PostMapping("/indices/refresh")
    public ResultData<List<IndexMeta>> refreshIndices(@RequestBody ElasticsearchConnectParam param) {
        return ResultData.success(aiAssistantService.refreshIndexCatalog(param));
    }

    /**
     * 获取知识库
     */
    @ApiOperation("获取知识库")
    @GetMapping("/knowledge-base")
    public ResultData<List<KnowledgeBaseItem>> getKnowledgeBase() {
        return ResultData.success(knowledgeBaseService.getAll());
    }

    /**
     * 保存知识库
     */
    @ApiOperation("保存知识库")
    @PostMapping("/knowledge-base")
    public ResultData<Void> saveKnowledgeBase(@RequestBody List<KnowledgeBaseItem> items) {
        knowledgeBaseService.save(items);
        return ResultData.success();
    }

    /**
     * AI 自动生成知识库草稿
     */
    @ApiOperation("AI生成知识库草稿")
    @PostMapping("/generate-kb-draft")
    public ResultData<List<KnowledgeBaseItem>> generateKbDraft(@RequestBody ElasticsearchConnectParam param) {
        return ResultData.success(aiAssistantService.generateKnowledgeBaseDraft(param));
    }

    /**
     * 获取热查询
     */
    @ApiOperation("获取热查询")
    @PostMapping("/hot-queries")
    public ResultData<List<Map<String, Object>>> getHotQueries(@RequestBody ElasticsearchConnectParam param) {
        return ResultData.success(historyService.getHotQueries(param, 10));
    }

    /**
     * 搜索相似查询
     */
    @ApiOperation("搜索相似查询")
    @PostMapping("/similar-queries")
    public ResultData<List<Map<String, Object>>> searchSimilarQueries(
            @RequestParam String query,
            @RequestBody ElasticsearchConnectParam param) {
        return ResultData.success(historyService.searchSimilarQueries(param, query, 5));
    }

    private ResultData<AIChatResponse> buildResult(AIChatResponse response) {
        if (response.isSuccess()) {
            return ResultData.success(response);
        } else {
            return ResultData.error(response);
        }
    }
}
