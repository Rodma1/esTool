package com.chen.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.service.elasticsearch.ElasticsearchCurlClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * AI 查询历史服务（使用 ES 自身存储）
 */
@Slf4j
@Service
public class AIHistoryService {

    private static final String HISTORY_INDEX = ".es-tool-ai-history";

    /**
     * 确保历史索引存在
     */
    private void ensureIndexExists(ElasticsearchConnectParam param) {
        ElasticsearchCurlClient client = createClient(param);
        try {
            String health = client.get("/" + HISTORY_INDEX);
            log.debug("历史索引已存在");
        } catch (Exception e) {
            // 索引不存在，创建
            createHistoryIndex(client);
        } finally {
            client.close();
        }
    }

    private void createHistoryIndex(ElasticsearchCurlClient client) {
        String mapping = "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"userQuery\": { \"type\": \"text\", \"analyzer\": \"standard\" },\n" +
                "      \"dsl\": { \"type\": \"keyword\", \"index\": false },\n" +
                "      \"indexPattern\": { \"type\": \"keyword\" },\n" +
                "      \"explanation\": { \"type\": \"text\", \"analyzer\": \"standard\" },\n" +
                "      \"sessionId\": { \"type\": \"keyword\" },\n" +
                "      \"success\": { \"type\": \"boolean\" },\n" +
                "      \"timestamp\": { \"type\": \"date\" },\n" +
                "      \"corrections\": { \"type\": \"nested\", \"properties\": { \"query\": {\"type\": \"text\"}, \"dsl\": {\"type\": \"keyword\"} } }\n" +
                "    }\n" +
                "  },\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1,\n" +
                "    \"number_of_replicas\": 0\n" +
                "  }\n" +
                "}";
        try {
            client.put("/" + HISTORY_INDEX, mapping);
            log.info("历史索引 {} 创建成功", HISTORY_INDEX);
        } catch (Exception e) {
            log.error("创建历史索引失败", e);
        }
    }

    /**
     * 保存查询历史
     */
    public void saveHistory(ElasticsearchConnectParam param, String userQuery, String dsl,
                            String indexPattern, String explanation, boolean success, String sessionId) {
        ensureIndexExists(param);
        ElasticsearchCurlClient client = createClient(param);
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("userQuery", userQuery);
            doc.put("dsl", dsl);
            doc.put("indexPattern", indexPattern);
            doc.put("explanation", explanation);
            doc.put("success", success);
            doc.put("sessionId", sessionId != null ? sessionId : UUID.randomUUID().toString());
            doc.put("timestamp", Instant.now().toString());
            doc.put("corrections", new ArrayList<>());

            String body = JSON.toJSONString(doc);
            client.post("/" + HISTORY_INDEX + "/_doc", body);
            log.debug("查询历史已保存");
        } catch (Exception e) {
            log.error("保存查询历史失败", e);
        } finally {
            client.close();
        }
    }

    /**
     * 搜索相似的成功查询（作为 few-shot 示例）
     */
    public List<Map<String, Object>> searchSimilarQueries(ElasticsearchConnectParam param, String userQuery, int size) {
//        ensureIndexExists(param);
        ElasticsearchCurlClient client = createClient(param);
        try {
            String searchBody = "{\n" +
                    "  \"query\": {\n" +
                    "    \"bool\": {\n" +
                    "      \"must\": [\n" +
                    "        { \"match\": { \"userQuery\": \"" + escapeJson(userQuery) + "\" } },\n" +
                    "        { \"term\": { \"success\": true } }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"sort\": [\n" +
                    "    { \"_score\": \"desc\" }\n" +
                    "  ],\n" +
                    "  \"size\": " + size + "\n" +
                    "}";

            String response = client.post("/" + HISTORY_INDEX + "/_search", searchBody);
            JSONObject root = JSON.parseObject(response);
            JSONArray hits = root.getJSONObject("hits").getJSONArray("hits");

            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < hits.size(); i++) {
                JSONObject source = hits.getJSONObject(i).getJSONObject("_source");
                Map<String, Object> item = new HashMap<>();
                item.put("userQuery", source.getString("userQuery"));
                item.put("dsl", source.getString("dsl"));
                item.put("indexPattern", source.getString("indexPattern"));
                item.put("explanation", source.getString("explanation"));
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.error("搜索相似查询失败", e);
            return new ArrayList<>();
        } finally {
            client.close();
        }
    }

    /**
     * 获取热查询（高频查询）
     */
    public List<Map<String, Object>> getHotQueries(ElasticsearchConnectParam param, int size) {
        ensureIndexExists(param);
        ElasticsearchCurlClient client = createClient(param);
        try {
            String searchBody = "{\n" +
                    "  \"query\": { \"term\": { \"success\": true } },\n" +
                    "  \"aggs\": {\n" +
                    "    \"hot_queries\": {\n" +
                    "      \"terms\": {\n" +
                    "        \"field\": \"userQuery.keyword\",\n" +
                    "        \"size\": " + size + "\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"size\": 0\n" +
                    "}";

            String response = client.post("/" + HISTORY_INDEX + "/_search", searchBody);
            JSONObject root = JSON.parseObject(response);
            JSONArray buckets = root.getJSONObject("aggregations").getJSONObject("hot_queries").getJSONArray("buckets");

            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < buckets.size(); i++) {
                JSONObject bucket = buckets.getJSONObject(i);
                Map<String, Object> item = new HashMap<>();
                item.put("query", bucket.getString("key"));
                item.put("count", bucket.getInteger("doc_count"));
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.error("获取热查询失败", e);
            return new ArrayList<>();
        } finally {
            client.close();
        }
    }

    private ElasticsearchCurlClient createClient(ElasticsearchConnectParam param) {
        return new ElasticsearchCurlClient(
                param.getHostName(), param.getPort(), param.getScheme(),
                param.getUserName(), param.getPassword()
        );
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
