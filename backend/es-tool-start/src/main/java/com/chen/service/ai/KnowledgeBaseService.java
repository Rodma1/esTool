package com.chen.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chen.model.ai.KnowledgeBaseItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库服务
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    @Value("${navigate.esConnectParamPath:.}")
    private String esConnectParamPath;

    private String knowledgeBasePath;
    private List<KnowledgeBaseItem> cache = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 知识库文件与 esConnectParam.json 放在同级目录
        File connectFile = new File(esConnectParamPath);
        File parentDir = connectFile.getParentFile();
        if (parentDir != null) {
            this.knowledgeBasePath = new File(parentDir, "esKnowledgeBase.json").getAbsolutePath();
        } else {
            this.knowledgeBasePath = "esKnowledgeBase.json";
        }
        load();
    }

    /**
     * 加载知识库
     */
    public synchronized List<KnowledgeBaseItem> load() {
        try {
            File file = new File(knowledgeBasePath);
            if (!file.exists()) {
                log.info("知识库文件不存在，返回空列表: {}", knowledgeBasePath);
                cache = new ArrayList<>();
                return cache;
            }
            String content = new String(Files.readAllBytes(Paths.get(knowledgeBasePath)), StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                cache = new ArrayList<>();
                return cache;
            }
            cache = JSON.parseObject(content, new TypeReference<List<KnowledgeBaseItem>>() {});
            log.info("知识库加载成功，共 {} 条", cache.size());
            return cache;
        } catch (IOException e) {
            log.error("加载知识库失败", e);
            cache = new ArrayList<>();
            return cache;
        }
    }

    /**
     * 保存知识库
     */
    public synchronized void save(List<KnowledgeBaseItem> items) {
        try {
            String json = JSON.toJSONString(items, true);
            Files.write(Paths.get(knowledgeBasePath), json.getBytes(StandardCharsets.UTF_8));
            cache = new ArrayList<>(items);
            log.info("知识库保存成功，共 {} 条", cache.size());
        } catch (IOException e) {
            log.error("保存知识库失败", e);
            throw new RuntimeException("保存知识库失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前缓存的知识库
     */
    public List<KnowledgeBaseItem> getAll() {
        return new ArrayList<>(cache);
    }

    /**
     * 根据关键词匹配相关条目（本地零 token 筛选）
     */
    public List<KnowledgeBaseItem> findRelevant(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerQuery = query.toLowerCase();
        List<KnowledgeBaseItem> result = new ArrayList<>();
        for (KnowledgeBaseItem item : cache) {
            if (matches(item, lowerQuery)) {
                result.add(item);
            }
        }
        return result;
    }

    private boolean matches(KnowledgeBaseItem item, String lowerQuery) {
        if (item.getBusinessName() != null && item.getBusinessName().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) {
            return true;
        }
        if (item.getIndices() != null) {
            for (String idx : item.getIndices()) {
                if (idx.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        if (item.getFields() != null) {
            for (String fieldName : item.getFields().keySet()) {
                if (fieldName.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
                KnowledgeBaseItem.FieldMeta meta = item.getFields().get(fieldName);
                if (meta != null && meta.getDescription() != null
                        && meta.getDescription().toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }
        return false;
    }
}
