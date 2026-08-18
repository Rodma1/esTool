package com.chen.service.operation7;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

/**
 * @Author chenyunzhi
 * @DATE 2026/2/12 14:24
 * @Description: 索引映射操作策略
 */
@Slf4j
public class IndexMappingOperationStrategy  implements ElasticsearchOperationStrategy {

    private final ElasticsearchFactoryParam factoryParam;

    public IndexMappingOperationStrategy(ElasticsearchFactoryParam factoryParam) {
        this.factoryParam = factoryParam;
    }
    @Override
    public Object execute(ElasticsearchClient client) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "QUERY":
                return this.queryIndexMapping(client, factoryParam.getIndexName());
            case "CREATE":
                return this.createIndexMapping(client, factoryParam.getIndexName(), factoryParam.getMapping());
            default:
                return true;
        }
    }

    public Object queryIndexMapping(ElasticsearchClient client, String indexName) throws IOException {
        GetMappingResponse getMappingResponse = client.indices().getMapping(g -> g.index(indexName));
        Map<String, IndexMappingRecord> result = getMappingResponse.result();
        return FastJsonUtils.convertToHashMap(result.get(indexName));
    }
    public Object createIndexMapping(ElasticsearchClient client, String indexName, String mapping) throws IOException {
        // 创建索引映射
        client.indices().putMapping(p -> p.index(indexName).withJson(new StringReader( mapping)));
        return true;
    }
}
