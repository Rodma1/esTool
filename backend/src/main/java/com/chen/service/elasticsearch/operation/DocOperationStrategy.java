package com.chen.service.elasticsearch.operation;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.chen.common.exception.ServiceException;
import com.chen.common.utils.StringUtils;
import com.chen.common.utils.date.DateTimeUtils;
import com.chen.domain.elsaticsearch.*;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/8 18:30
 * @Description:
 */
public class DocOperationStrategy implements ElasticsearchOperationStrategy {

    private  final ElasticsearchFactoryParam factoryParam;


    public DocOperationStrategy(ElasticsearchFactoryParam factoryParam) {
        this.factoryParam = factoryParam;
    }


    @Override
    public Object execute(ElasticsearchClient client) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "INSERT":
                return this.insertDoc(client, factoryParam.getIndexName(),factoryParam.getDocument());
            case  "DELETE":
                return this.deleteIndexDocument(client, factoryParam.getIndices(), factoryParam.getDocumentIds());
            case "INFO":
                return this.getDocumentInfo(client ,factoryParam.getIndexName(), factoryParam.getDocumentId());
            case "PAGE":
                return this.getDocumentsPage(client, factoryParam.getIndices(),factoryParam.getDocumentId(), factoryParam.getPageNum()
                        , factoryParam.getPageSize(), factoryParam.getSortField(), factoryParam.getSortOrder()
                        , factoryParam.getSearchFields(), factoryParam.getTimeSearch());
            case "COUNT":
                return this.getDocumentCount(client, factoryParam.getIndices());
            case "UPDATE":
                return this.updateByQuery(client, factoryParam.getDocumentIds(), factoryParam.getIndices(), factoryParam.getUpdateFields());
            default:
                return null;
        }
    }

    private Object insertDoc(ElasticsearchClient client, String indexName, String jsonContent) throws IOException {
        try {
            IndexResponse index = client.index(i -> i.index(indexName).withJson(new StringReader(jsonContent)));
            return index.id();
        } catch (Exception e) {
            throw  new ServiceException("创建失败" + e.getMessage());
        }

    }

    public void updateDocument(ElasticsearchClient client, String indexName,String documentId, String jsonContent) throws IOException {
    }

    public Object deleteDocument(ElasticsearchClient client, List<String> documentIds) throws IOException {
        try {
            DeleteByQueryResponse deleteByQueryResponse = client.deleteByQuery(d -> d.query(q -> q.ids(i -> i.values(documentIds))));
            return deleteByQueryResponse.deleted();

        } catch (Exception e) {
            throw  new ServiceException(e.getMessage());
        }
    }

    /**
     * 批量删除文档
     */
    public Object deleteIndexDocument(ElasticsearchClient client,  List<String> indices, List<String> documentIds) throws IOException {
        try {
            DeleteByQueryResponse deleteByQueryResponse = client.deleteByQuery(d -> d.index(indices).query(q -> q.ids(i -> i.values(documentIds))));
            return deleteByQueryResponse.deleted();

        } catch (Exception e) {
            throw  new ServiceException(e.getMessage());
        }
    }

    public Object getDocumentInfo(ElasticsearchClient client, String indexName , String documentId) throws IOException {

        return client.get(g -> g
                        .index(indexName)
                        .id(documentId)
                , HashMap.class
        );
    }

    /**
     * 获取分页的文档列表
     */
    public Object getDocumentsPage(ElasticsearchClient client, List<String> indices, String documentId
            , int pageNum, int pageSize, String sortField, String sortOrder, List<SearchFields> searchFields, ElasticsearchTimeSearch timeSearch) throws IOException {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        CountRequest.Builder countBuilder = new CountRequest.Builder();
        if (ObjectUtil.isNotNull(indices) && !indices.isEmpty()) {
            builder.index(indices);
            countBuilder.index(indices);
        }
        builder.from((pageNum - 1) * pageSize).size(pageSize);

        List<ElasticsearchDocument> elasticsearchDocuments = new ArrayList<>();
        // 根据id精确查询
        if (StringUtils.isNotBlank(documentId) && documentId.length() != 0) {
            List<Hit<HashMap>> hits = client.search(s -> s.index(indices).query(q -> q.term(t -> t.field("_id").value(documentId))), HashMap.class).hits().hits();
            if (hits.isEmpty()) {
                return elasticsearchDocuments;
            }
            Hit<HashMap> hashMapGetResponse = hits.stream().findFirst().get();

            ElasticsearchDocument elasticsearchDocument = new ElasticsearchDocument();
            elasticsearchDocument.setId(hashMapGetResponse.id()).setIndex(hashMapGetResponse.index()).setSource(hashMapGetResponse.source());
            elasticsearchDocuments.add(elasticsearchDocument);
            ElasticsearchDocumentPage elasticsearchDocumentPage = new ElasticsearchDocumentPage();
            elasticsearchDocumentPage.setRows(elasticsearchDocuments).setCount(1L);

            return  elasticsearchDocumentPage;
        }

        builder.query(q -> q.bool(b -> b.filter(this.searchFilter(searchFields, timeSearch)))).ignoreUnavailable(true);
        countBuilder.query(q -> q.bool(b -> b.filter(this.searchFilter(searchFields, timeSearch)))).ignoreUnavailable(true);
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            builder.sort(sort->sort.field(f->f.field(sortField).order(SortOrder.valueOf(sortOrder))));
        }

        client.search(builder.build(), HashMap.class).hits().hits().forEach(item ->{
            ElasticsearchDocument elasticsearchDocument = new ElasticsearchDocument();
            elasticsearchDocument.setId(item.id()).setIndex(item.index()).setSource(item.source());
            elasticsearchDocuments.add(elasticsearchDocument);
        });
        ElasticsearchDocumentPage elasticsearchDocumentPage = new ElasticsearchDocumentPage();
        elasticsearchDocumentPage.setRows(elasticsearchDocuments).setCount(client.count(countBuilder.build()).count());

        return elasticsearchDocumentPage;
    }

    /**
     * 分页查询构造过滤条件
     */
    private List<Query> searchFilter(List<SearchFields> searchFields, ElasticsearchTimeSearch timeSearch) {
        // 存储查询条件
        List<Query> filterQuery = new ArrayList<>();
        // 判断是否需要加入时间条件查询
        if (ObjectUtil.isNotNull(timeSearch) && StringUtils.isNotBlank(timeSearch.getBeginTime()) && StringUtils.isNotBlank(timeSearch.getEndTime()) && StringUtils.isNotBlank(timeSearch.getField()) ) {
            long begin = DateTimeUtils.toDate(timeSearch.getBeginTime(), DateTimeUtils.y4M2d2H2m2s2).getTime();
            long end = DateTimeUtils.toDate(timeSearch.getEndTime(), DateTimeUtils.y4M2d2H2m2s2).getTime();
            Query rangeQuery = RangeQuery.of(rangeQueryBuilder -> rangeQueryBuilder.field(timeSearch.getField()).gte(JsonData.of(begin)).lte(JsonData.of(end)))._toQuery();
            filterQuery.add(rangeQuery);
        }

        if (ObjectUtil.isNotNull(searchFields)) {
            for (SearchFields searchField: searchFields) {
                filterQuery.add(TermQuery.of(m -> m.field(searchField.getKey()).value(searchField.getValue()))._toQuery());
            }
        }

        return filterQuery;
    }

    /**
     * 获取文档数
     */
    public Object getDocumentCount(ElasticsearchClient client, List<String> indices) throws IOException {
        if (ObjectUtil.isNotNull(indices) && !indices.isEmpty()) {
            return client.count(c -> c.index(indices)).count();
        }
        return client.count().count();
    }

    /**
     * 批量更新文档
     */
    public Object updateByQuery(ElasticsearchClient client, List<String> documentIds, List<String> indices, List<UpdateFields> updateFields) throws IOException {
        if (ObjectUtil.isNotNull(documentIds) && !documentIds.isEmpty() && ObjectUtil.isNotNull(indices) && !indices.isEmpty() && ObjectUtil.isNotNull(updateFields) && !updateFields.isEmpty() ) {
            StringBuilder stringBuilder = new StringBuilder();

            for (UpdateFields updateField : updateFields) {
                Object value = updateField.getValue();
                stringBuilder.append("ctx._source.")
                        .append(updateField.getKey())
                        .append(" = ");

                if (value instanceof String) {
                    stringBuilder.append("'").append(value).append("'");
                } else {
                    stringBuilder.append(value);
                }

                stringBuilder.append("; ");
            }
            UpdateByQueryResponse updateByQueryResponse = client.updateByQuery(u -> u.index(indices).query(q -> q.ids(i -> i.values(documentIds))).script(s -> s.inline(i -> i.source(stringBuilder.toString()))));
            return updateByQueryResponse.toString();
        }
        return true;
    }


}
