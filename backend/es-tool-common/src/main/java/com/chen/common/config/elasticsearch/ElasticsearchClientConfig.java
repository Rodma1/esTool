package com.chen.common.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

public interface ElasticsearchClientConfig {

    /**
     * 获取单例的 ElasticsearchClient 实例
     * @return
     */
    ElasticsearchClient elasticsearchClient();

    /**
     * 关闭 ElasticsearchClient 实例
     */
    void close();

    /**
     * 注册一个关闭钩子，在 JVM 退出时自动调用 close 方法关闭 ElasticsearchClient 实例，释放资源。
     */
    void addShutdownHook();


}
