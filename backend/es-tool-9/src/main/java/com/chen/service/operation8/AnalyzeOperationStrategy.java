package com.chen.service.operation8;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.config.NavigateConfig;
import com.chen.common.utils.StringUtils;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.common.utils.json.ReadJsonUtils;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

import java.io.IOException;

public class AnalyzeOperationStrategy  implements ElasticsearchOperationStrategy {
    private  final ElasticsearchFactoryParam factoryParam;

    public AnalyzeOperationStrategy(ElasticsearchFactoryParam factoryParam) {
        this.factoryParam = factoryParam;
    }

    @Override
    public Object execute(ElasticsearchClient elasticsearchClient) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "PLUGINS":
                return this.pluginsInfo(elasticsearchClient);
            case "ANALYZERS":
                return this.analyzers();
            case "ANALYZE":
                return this.analyze(elasticsearchClient, factoryParam.getIndexName(), factoryParam.getAnalyzer(), factoryParam.getDocument(), factoryParam.getField());
        }
        return null;
    }

    /**
     * 查看安装插件
     */
    public Object pluginsInfo(ElasticsearchClient client) throws IOException {
        return FastJsonUtils.convertToHashMapList(client.cat().plugins().plugins());
    }

    /**
     * 可以使用的分词器
     */
    public Object analyzers() throws IOException {
        return FastJsonUtils.toObject(ReadJsonUtils.readJsonFile(NavigateConfig.getAnalyzersParamPath()));
    }

    /**
     * 分词器分词
     */
    public Object analyze(ElasticsearchClient client, String index, String analyzer, String text, String field) throws IOException {
        if (StringUtils.isBlank(index)) {
            return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.analyzer(analyzer).text(text)).tokens());
        }
        if (StringUtils.isBlank(analyzer) && StringUtils.isBlank(field)) {
            return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.index(index).text(text)).tokens());
        }
        if (StringUtils.isBlank(field)) {
            return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.index(index).analyzer(analyzer).text(text)).tokens());
        }
        if (StringUtils.isBlank(analyzer)) {
            return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.index(index).field(field).text(text)).tokens());
        }
        return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.index(index).analyzer(analyzer).text(text)).tokens());
    }
}
