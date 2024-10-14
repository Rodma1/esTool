package com.chen.service.elasticsearch.operation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.aliases.AliasesRecord;
import co.elastic.clients.elasticsearch.indices.DeleteAliasResponse;
import co.elastic.clients.elasticsearch.indices.PutAliasRequest;
import co.elastic.clients.elasticsearch.indices.PutAliasResponse;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import com.chen.common.utils.BeanUtils;
import com.chen.domain.elsaticsearch.CatAliasesRecord;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/11 17:06
 * @Description:
 */
public class AliasesOperationStrategy  implements ElasticsearchOperationStrategy {

    private  final ElasticsearchFactoryParam factoryParam;

    public AliasesOperationStrategy(ElasticsearchFactoryParam factoryParam) {
        this.factoryParam = factoryParam;
    }


    @Override
    public Object execute(ElasticsearchClient elasticsearchClient) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "INSERT":
                return this.association(elasticsearchClient, factoryParam.getAlias(), factoryParam.getIndices());
            case "LIST":
                return this.getAliasList(elasticsearchClient);
            case "DISASSOCIATION":
                return this.disassociation(elasticsearchClient, factoryParam.getAlias(), factoryParam.getIndices());
            case "WRITE_INDEX":
                return this.updateAliasWriteIndex(elasticsearchClient, factoryParam.getAlias(), factoryParam.getIndexName());
            default:
                return null;
        }

    }

    /**
     * 定义别名并关联多个索引，并且可以指定某个索引为写入索引
     */
    private Object association(ElasticsearchClient client, String alias, List<String> indices) throws IOException {

        PutAliasRequest build = new PutAliasRequest.Builder().index(indices).name(alias).isWriteIndex(false).build();

        PutAliasResponse putAliasResponse = client.indices().putAlias(build);
        return putAliasResponse.acknowledged();
    }

    /**
     * 获取别名和索引的关系列表
     */
    private Object getAliasList(ElasticsearchClient client) throws IOException {
        List<AliasesRecord> aliasesRecords = client.cat().aliases().valueBody();
        return BeanUtils.copyList(aliasesRecords, CatAliasesRecord.class);
    }

    /**
     * 别名和索引取消关联
     */
    private Object disassociation(ElasticsearchClient client, String alias, List<String> indices) throws IOException {
        DeleteAliasResponse deleteAliasResponse = client.indices().deleteAlias(d -> d.name(alias).index(indices));
        return deleteAliasResponse.acknowledged();
    }

    /**
     * 更新索引为别名的写入索引
     */
    private Object updateAliasWriteIndex(ElasticsearchClient client, String alias, String index) throws IOException {
        try {
            UpdateAliasesResponse updateAliasesResponse = client.indices()
                    .updateAliases(u -> u.actions(a -> a.remove(r -> r.index(index).alias(alias)))
                            .actions(a -> a.add(add -> add.index(index).alias(alias).isWriteIndex(true))));
            return updateAliasesResponse.acknowledged();
        } catch (Exception e) {
            throw new ServerException("更新失败" + e.getMessage());
        }

    }


}
