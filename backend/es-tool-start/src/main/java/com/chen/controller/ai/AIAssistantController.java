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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 助手控制器
 */
@RequiredArgsConstructor
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
