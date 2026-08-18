package com.chen.model.ai;

import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import lombok.Data;

import java.util.List;

/**
 * AI 对话请求
 */
@Data
public class AIChatRequest {

    /**
     * 对话消息列表（可为空，默认只发最后一条 user 消息）
     */
    private List<ChatMessage> messages;

    /**
     * 用户当前输入（如果 messages 为空，则以此构造单轮对话）
     */
    private String userInput;

    /**
     * ES 连接参数（可选，用于带上下文场景）
     */
    private ElasticsearchConnectParam connectParam;

    /**
     * 模式：chat / query / execute
     */
    private String mode = "chat";
}
