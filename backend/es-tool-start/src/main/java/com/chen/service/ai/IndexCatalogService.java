package com.chen.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.model.ai.IndexMeta;
import com.chen.service.elasticsearch.ElasticsearchCurlClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 索引目录服务：拉取并缓存 ES 索引元数据
 */
@Slf4j
@Service
public class IndexCatalogService {

    /**
     * 连接级别缓存：connectKey -> 索引列表
     */
    private final Map<String, List<IndexMeta>> catalogCache = new ConcurrentHashMap<>();

    /**
     * 构建缓存 key
     */
    private String buildKey(ElasticsearchConnectParam param) {
        return param.getScheme() + "://" + param.getHostName() + ":" + param.getPort();
    }

    /**
     * 获取索引目录（带缓存）
     */
    public List<IndexMeta> getIndexCatalog(ElasticsearchConnectParam param) {
        String key = buildKey(param);
        List<IndexMeta> cached = catalogCache.get(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        return refreshIndexCatalog(param);
    }

    /**
     * 刷新索引目录
     */
    public synchronized List<IndexMeta> refreshIndexCatalog(ElasticsearchConnectParam param) {
        String key = buildKey(param);
        log.info("正在刷新索引目录: {}", key);

        ElasticsearchCurlClient client = new ElasticsearchCurlClient(
                param.getHostName(), param.getPort(), param.getScheme(),
                param.getUserName(), param.getPassword()
        );

        try {
            // 1. 获取索引列表
            String indicesJson = client.getIndices();
            JSONArray indicesArray = JSON.parseArray(indicesJson);

            List<IndexMeta> result = new ArrayList<>();
            if (indicesArray == null) {
                catalogCache.put(key, result);
                return result;
            }

            // 2. 逐个获取 mapping（限制数量，避免太大）
            int limit = Math.min(indicesArray.size(), 200);
            for (int i = 0; i < limit; i++) {
                JSONObject idxObj = indicesArray.getJSONObject(i);
                String indexName = idxObj.getString("index");
                if (indexName == null || indexName.startsWith(".")) {
                    continue; // 跳过系统索引
                }

                IndexMeta meta = new IndexMeta();
                meta.setName(indexName);
                meta.setAlias(idxObj.getString("alias"));
                meta.setDocCount(idxObj.getString("docs.count"));
                meta.setSize(idxObj.getString("store.size"));

                // 获取字段摘要
                try {
                    String mappingJson = client.getMapping(indexName);
                    meta.setFieldSummary(extractFieldSummary(mappingJson, indexName));
                } catch (Exception e) {
                    log.warn("获取索引 {} 的 mapping 失败: {}", indexName, e.getMessage());
                    meta.setFieldSummary(new HashMap<>());
                }

                result.add(meta);
            }

            catalogCache.put(key, result);
            log.info("索引目录刷新成功，共 {} 个索引", result.size());
            return result;
        } catch (Exception e) {
            log.error("刷新索引目录失败", e);
            throw new RuntimeException("刷新索引目录失败: " + e.getMessage());
        } finally {
            client.close();
        }
    }

    /**
     * 从 mapping JSON 中提取字段摘要
     */
    private Map<String, String> extractFieldSummary(String mappingJson, String indexName) {
        Map<String, String> summary = new HashMap<>();
        try {
            JSONObject root = JSON.parseObject(mappingJson);
            JSONObject indexObj = root.getJSONObject(indexName);
            if (indexObj == null) {
                // 有时返回的 key 不是索引名，尝试第一个 key
                for (String key : root.keySet()) {
                    indexObj = root.getJSONObject(key);
                    break;
                }
            }
            if (indexObj == null) return summary;

            JSONObject mappings = indexObj.getJSONObject("mappings");
            if (mappings == null) return summary;

            // ES 7.x+ 可能是 properties 直接挂在 mappings 下，也可能是 type -> properties
            JSONObject properties = mappings.getJSONObject("properties");
            if (properties == null) {
                for (String typeKey : mappings.keySet()) {
                    JSONObject typeObj = mappings.getJSONObject(typeKey);
                    if (typeObj != null && typeObj.containsKey("properties")) {
                        properties = typeObj.getJSONObject("properties");
                        break;
                    }
                }
            }

            if (properties != null) {
                for (String fieldName : properties.keySet()) {
                    JSONObject fieldObj = properties.getJSONObject(fieldName);
                    if (fieldObj != null) {
                        String type = fieldObj.getString("type");
                        if (type != null) {
                            summary.put(fieldName, type);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析 mapping 失败: {}", e.getMessage());
        }
        return summary;
    }

    /**
     * 本地关键词匹配，找出相关索引（零 token 成本）
     */
    public List<IndexMeta> findRelevantIndices(String query, ElasticsearchConnectParam param) {
        List<IndexMeta> all = getIndexCatalog(param);
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerQuery = query.toLowerCase();
        List<IndexMeta> result = new ArrayList<>();
        for (IndexMeta meta : all) {
            if (matches(meta, lowerQuery)) {
                result.add(meta);
            }
        }
        // 最多返回 5 个
        return result.size() > 5 ? result.subList(0, 5) : result;
    }

    private boolean matches(IndexMeta meta, String lowerQuery) {
        if (meta.getName() != null && meta.getName().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (meta.getAlias() != null && meta.getAlias().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (meta.getFieldSummary() != null) {
            for (Map.Entry<String, String> entry : meta.getFieldSummary().entrySet()) {
                if (entry.getKey().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        return false;
    }
}
