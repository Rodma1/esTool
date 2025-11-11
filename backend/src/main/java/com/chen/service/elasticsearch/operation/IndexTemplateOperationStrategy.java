package com.chen.service.elasticsearch.operation;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.component_templates.ComponentTemplate;
import co.elastic.clients.elasticsearch.cat.templates.TemplatesRecord;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.get_index_template.IndexTemplateItem;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.JsonpSerializable;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.chen.common.exception.ServiceException;
import com.chen.common.utils.BeanUtils;
import com.chen.common.utils.StringUtils;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.domain.elsaticsearch.CatTemplatesRecord;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/16 19:14
 * @Description:
 */
@RequiredArgsConstructor
public class IndexTemplateOperationStrategy implements ElasticsearchOperationStrategy {

    private final ElasticsearchFactoryParam factoryParam;
    @Override
    public Object execute(ElasticsearchClient elasticsearchClient) throws IOException {
        switch (factoryParam.getOperationType()) {
            case "LIST":
                return this.getTemplateList(elasticsearchClient, factoryParam.getIndexTemplate());
            case "INFO":
                return this.getTemplateInfo(elasticsearchClient, factoryParam.getIndexTemplate());
            case "PUT":
                return this.putTemplate(elasticsearchClient, factoryParam.getIndexTemplate(), factoryParam.getIndexTemplateContent());
            case "DELETE":
                return this.deleteTemplate(elasticsearchClient, factoryParam.getIndexTemplate());
            default:
                return null;
        }

    }

    /**
     * 获取模板列表
     */
//    public Object getTemplateList(ElasticsearchClient client, String indexTemplate) throws IOException {
//        GetIndexTemplateRequest.Builder builder = new GetIndexTemplateRequest.Builder();
//        if (StringUtils.isNotBlank(indexTemplate)) {
//            builder.name("*" + indexTemplate + "*");
//        }
//
//        List<IndexTemplateItem> indexTemplateItems = client.indices().getIndexTemplate(builder.build()).indexTemplates();
//        List<Object> objects = new ArrayList<>();
//        indexTemplateItems.forEach(item-> {
//            HashMap<String, Object> stringObjectHashMap = new HashMap<>();
//            stringObjectHashMap.put("name",item.name());
//            stringObjectHashMap.put("indexPatterns",item.indexTemplate().indexPatterns());
//            stringObjectHashMap.put("composedOf",item.indexTemplate().composedOf());
//            objects.add(stringObjectHashMap);
//        });
//        return objects;
//    }
    public Object getTemplateList(ElasticsearchClient client, String indexTemplate) throws IOException {
        RestClient restClient = ((RestClientTransport) client._transport()).restClient();

        String url = "/_template";
        if (StringUtils.isNotBlank(indexTemplate)) {
            url += "/" + indexTemplate;
        }

        Request request = new Request("GET", url);
        Response response = restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        List<Object> objects = new ArrayList<>();

        // 遍历每个模板
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String templateName = entry.getKey();
            JsonNode templateNode = entry.getValue();

            Map<String, Object> map = new HashMap<>();
            map.put("name", templateName);

            // indexPatterns 在 7.x 叫 index_patterns
            if (templateNode.has("index_patterns")) {
                map.put("indexPatterns", mapper.convertValue(templateNode.get("index_patterns"), List.class));
            } else {
                map.put("indexPatterns", Collections.emptyList());
            }

            // composedOf 在 7.x 没有，可以为空（或者放 mappings/settings）
            if (templateNode.has("composed_of")) {
                map.put("composedOf", mapper.convertValue(templateNode.get("composed_of"), List.class));
            } else {
                map.put("composedOf", Collections.emptyList());
            }

            objects.add(map);
        }

        return objects;
    }
    /**
     * 查询模板详情
     */
    public Object getTemplateInfo(ElasticsearchClient client, String indexTemplate) throws IOException {
        IndexTemplateItem indexTemplateItem = client.indices().getIndexTemplate(t -> t.name(indexTemplate)).indexTemplates().get(0);
        return FastJsonUtils.convertToHashMap(indexTemplateItem);

    }

    /**
     * 创建和更新
     */
    public Object putTemplate(ElasticsearchClient client, String indexTemplate, String indexTemplateContent) {
        try {
            PutIndexTemplateResponse putIndexTemplateResponse = client.indices().putIndexTemplate(p -> p.name(indexTemplate).withJson(new StringReader(indexTemplateContent)));
            return putIndexTemplateResponse.acknowledged();
        } catch (Exception e) {
            throw new ServiceException("更新失败:" + e.getMessage());
        }

    }

    /**
     * 删除模板
     */
    public Object deleteTemplate(ElasticsearchClient client, String indexTemplate) {
        try {
            DeleteIndexTemplateResponse deleteIndexTemplateResponse = client.indices().deleteIndexTemplate(d -> d.name(indexTemplate));
            return deleteIndexTemplateResponse.acknowledged();
        } catch (Exception e) {
            throw  new ServiceException("删除失败:"  + e.getMessage());
        }
    }




}
