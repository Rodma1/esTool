package com.chen.model.ai;

import lombok.Data;

/**
 * AI 对话响应
 */
@Data
public class AIChatResponse {

    /**
     * AI 回复内容
     */
    private String reply;

    /**
     * 生成的 DSL（query/execute 模式时返回）
     */
    private String dsl;

    /**
     * 索引匹配模式
     */
    private String indexPattern;

    /**
     * 自然语言解释
     */
    private String explanation;

    /**
     * ES 执行结果（execute 模式时返回）
     */
    private Object executionResult;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 消耗的 token 数（估算）
     */
    private Integer tokenUsage;

    public static AIChatResponse success(String reply) {
        AIChatResponse response = new AIChatResponse();
        response.setReply(reply);
        response.setSuccess(true);
        return response;
    }

    public static AIChatResponse error(String errorMessage) {
        AIChatResponse response = new AIChatResponse();
        response.setErrorMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
