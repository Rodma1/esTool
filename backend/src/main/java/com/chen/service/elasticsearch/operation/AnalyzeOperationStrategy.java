package com.chen.service.elasticsearch.operation;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.utils.StringUtils;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

import java.io.IOException;
import java.util.HashMap;

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
                return this.analyze(elasticsearchClient, factoryParam.getIndexName(), factoryParam.getAnalyzer(), factoryParam.getDocument());
        }
        return null;
    }

    /**
     * 查看安装插件
     */
    public Object pluginsInfo(ElasticsearchClient client) throws IOException {
        return FastJsonUtils.convertToHashMapList(client.cat().plugins().valueBody());
    }

    /**
     * 可以使用的分词器
     */
    public Object analyzers() throws IOException {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("jieba_index", "jieba_index");
        stringStringHashMap.put("jieba_search", "jieba_search");
        stringStringHashMap.put("ik_smart", "ik_smart");
        stringStringHashMap.put("ik_max_word", "ik_max_word");
        stringStringHashMap.put("standard", "standard");
        return stringStringHashMap;
    }

    /**
     * 分词器分词
     */
    public Object analyze(ElasticsearchClient client, String index, String analyzer, String text) throws IOException {
        if (StringUtils.isBlank(index)) {
            return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.analyzer(analyzer).text(text)).tokens());
        }
        return FastJsonUtils.convertToHashMapList(client.indices().analyze(a -> a.index(index).analyzer(analyzer).text(text)).tokens());
    }
}
