package com.chen.model.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 知识库条目
 */
@Data
public class KnowledgeBaseItem {

    /**
     * 中文业务名
     */
    private String businessName;

    /**
     * 对应索引名称列表
     */
    private List<String> indices;

    /**
     * 业务描述
     */
    private String description;

    /**
     * 字段说明：key=字段名, value={type, description}
     */
    private Map<String, FieldMeta> fields;

    @Data
    public static class FieldMeta {
        /**
         * 字段类型
         */
        private String type;

        /**
         * 字段中文说明
         */
        private String description;

        /**
         * 是否分词（text 类型通常为 true，keyword 为 false）
         */
        private Boolean analyzed;
    }
}
