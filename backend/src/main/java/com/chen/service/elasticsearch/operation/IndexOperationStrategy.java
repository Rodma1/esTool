package com.chen.service.elasticsearch.operation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.chen.common.utils.BeanUtils;
import com.chen.domain.elsaticsearch.CatIndicesRecord;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/6/24 17:24
 * @Description: es索引操作
 */
@Slf4j
public class IndexOperationStrategy implements ElasticsearchOperationStrategy {

    private  final ElasticsearchFactoryParam factoryParam;

    public IndexOperationStrategy(ElasticsearchFactoryParam factoryParam) {
        this.factoryParam = factoryParam;
    }
    @Override
    public Object execute(ElasticsearchClient client) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "BATCH_DELETE":
                return this.batchDeleteIndexList(client, factoryParam.getIndices());
            case "INDEX_LIST":
                return this.catIndicesList(client);
            case "CREATE":
                return this.createIndex(client, factoryParam.getIndexName());
            default:
                return true;
        }

    }

    /**
     * 批量删除多个索引
     * @param indices 索引列表
     */
    private Object batchDeleteIndexList(ElasticsearchClient client, List<String> indices) {
        try {
            DeleteIndexResponse delete = client.indices().delete(i -> i.index(indices));
            System.out.println(delete.acknowledged());
        } catch (Exception e) {
            log.error("删除索引失败");
        }
        return "执行成功";
    }

    /**
     * 获取所有索引
     */
    private Object catIndicesList(ElasticsearchClient client) throws IOException {
        List<IndicesRecord> indicesRecords = client.cat().indices().valueBody();
        return BeanUtils.copyList(indicesRecords, CatIndicesRecord.class);
    }

    /**
     * 创建索引
     */
    private Object createIndex(ElasticsearchClient client,String indexName) throws IOException {
        CreateIndexResponse createIndexResponse = client.indices().create(c -> c.index(indexName));
        return createIndexResponse.index();
    }


}
