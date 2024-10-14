package com.chen.domain.elsaticsearch;

import lombok.Data;

/**
 * @Author chenyunzhi
 * @DATE 2024/9/30 17:22
 * @Description: 通过时间查询文档
 */
@Data
public class ElasticsearchTimeSearch {


    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 时间字段
     */
    private String field;
}
