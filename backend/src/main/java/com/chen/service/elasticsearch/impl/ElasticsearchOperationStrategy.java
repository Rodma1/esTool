package com.chen.service.elasticsearch.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;

/**
 * @Author chenyunzhi
 * @DATE 2024/6/24 17:17
 * @Description: 定义抽象策略类  负责es操作
 */
public interface ElasticsearchOperationStrategy {

    /**
     * 要执行具体操作的接口
     */
    Object execute(ElasticsearchClient elasticsearchClient) throws IOException;
}
