package com.chen.model.ai;

import lombok.Data;

import java.util.Map;

/**
 * 索引轻量元数据
 */
@Data
public class IndexMeta {

    /**
     * 索引名
     */
    private String name;

    /**
     * 别名列表
     */
    private String alias;

    /**
     * 文档数
     */
    private String docCount;

    /**
     * 存储大小
     */
    private String size;

    /**
     * 字段摘要：fieldName -> type
     */
    private Map<String, String> fieldSummary;
}
