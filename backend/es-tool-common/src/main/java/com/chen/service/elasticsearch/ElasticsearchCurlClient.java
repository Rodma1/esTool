package com.chen.service.elasticsearch;

import com.chen.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch Curl 客户端工具类
 * 通过模拟curl命令的方式直接调用Elasticsearch REST API
 *
 * @author chen
 */
@Slf4j
public class ElasticsearchCurlClient {

    private final String host;
    private final int port;
    private final String scheme;
    private final String username;
    private final String password;
    private final CloseableHttpClient httpClient;

    /**
     * 构造函数
     *
     * @param host     ES主机地址
     * @param port     ES端口
     * @param scheme   协议(http/https)
     * @param username 用户名
     * @param password 密码
     */
    public ElasticsearchCurlClient(String host, int port, String scheme, String username, String password) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        this.username = username;
        this.password = password;
        this.httpClient = createHttpClient();
    }

    /**
     * 创建HTTP客户端
     *
     * @return CloseableHttpClient
     */
    private CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();
    }

    /**
     * 执行GET请求 (类似 curl -X GET)
     *
     * @param endpoint API端点
     * @return 响应结果
     */
    public String get(String endpoint) {
        return executeRequest("GET", endpoint, null);
    }

    /**
     * 执行POST请求 (类似 curl -X POST)
     *
     * @param endpoint API端点
     * @param body     请求体
     * @return 响应结果
     */
    public String post(String endpoint, String body) {
        return executeRequest("POST", endpoint, body);
    }

    /**
     * 执行PUT请求 (类似 curl -X PUT)
     *
     * @param endpoint API端点
     * @param body     请求体
     * @return 响应结果
     */
    public String put(String endpoint, String body) {
        return executeRequest("PUT", endpoint, body);
    }

    /**
     * 执行DELETE请求 (类似 curl -X DELETE)
     *
     * @param endpoint API端点
     * @return 响应结果
     */
    public String delete(String endpoint) {
        return executeRequest("DELETE", endpoint, null);
    }

    /**
     * 执行HEAD请求 (类似 curl -I)
     *
     * @param endpoint API端点
     * @return 响应头信息
     */
    public Map<String, String> head(String endpoint) {
        String url = buildUrl(endpoint);
        HttpHead request = new HttpHead(url);
        addAuthHeader(request);
        
        try {
            HttpResponse response = httpClient.execute(request);
            Map<String, String> headers = new HashMap<>();
            
            // 获取所有响应头
            for (org.apache.http.Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            
            return headers;
        } catch (IOException e) {
            log.error("执行HEAD请求失败: {}", url, e);
            throw new ServiceException("执行HEAD请求失败: " + e.getMessage());
        }
    }

    /**
     * 执行搜索请求 (类似 curl -X GET "localhost:9200/index/_search")
     *
     * @param index 索引名称
     * @param query 查询DSL
     * @return 搜索结果
     */
    public String search(String index, String query) {
        String endpoint = "/" + index + "/_search";
        return post(endpoint, query);
    }

    /**
     * 获取索引映射 (类似 curl -X GET "localhost:9200/index/_mapping")
     *
     * @param index 索引名称
     * @return 映射信息
     */
    public String getMapping(String index) {
        String endpoint = "/" + index + "/_mapping";
        return get(endpoint);
    }

    /**
     * 创建索引 (类似 curl -X PUT "localhost:9200/index")
     *
     * @param index 索引名称
     * @param body  索引配置
     * @return 创建结果
     */
    public String createIndex(String index, String body) {
        String endpoint = "/" + index;
        return put(endpoint, body);
    }

    /**
     * 删除索引 (类似 curl -X DELETE "localhost:9200/index")
     *
     * @param index 索引名称
     * @return 删除结果
     */
    public String deleteIndex(String index) {
        String endpoint = "/" + index;
        return delete(endpoint);
    }

    /**
     * 获取集群健康状态 (类似 curl -X GET "localhost:9200/_cluster/health")
     *
     * @return 集群健康信息
     */
    public String getClusterHealth() {
        return get("/_cluster/health");
    }

    /**
     * 获取节点信息 (类似 curl -X GET "localhost:9200/_nodes")
     *
     * @return 节点信息
     */
    public String getNodesInfo() {
        return get("/_nodes");
    }

    /**
     * 获取所有索引 (类似 curl -X GET "localhost:9200/_cat/indices?v")
     *
     * @return 索引列表
     */
    public String getIndices() {
        return get("/_cat/indices?v&format=json");
    }

    /**
     * 执行通用HTTP请求
     *
     * @param method   HTTP方法
     * @param endpoint API端点
     * @param body     请求体
     * @return 响应结果
     */
    public String executeRequest(String method, String endpoint, String body) {
        String url = buildUrl(endpoint);
        HttpRequestBase request;
        
        try {
            switch (method.toUpperCase()) {
                case "GET":
                    request = new HttpGet(url);
                    break;
                case "POST":
                    request = new HttpPost(url);
                    if (body != null) {
                        ((HttpPost) request).setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                    }
                    break;
                case "PUT":
                    request = new HttpPut(url);
                    if (body != null) {
                        ((HttpPut) request).setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                    }
                    break;
                case "DELETE":
                    request = new HttpDelete(url);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的HTTP方法: " + method);
            }
            
            // 添加认证头和内容类型
            addAuthHeader(request);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            
            // 执行请求
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            // 读取响应体
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";
            
            log.debug("ES请求: {} {} -> 状态码: {}, 响应: {}", method, url, statusCode, responseBody);
            
            // 检查状态码
            if (statusCode >= 200 && statusCode < 300) {
                return responseBody;
            } else {
                throw new ServiceException("ES请求失败 [" + statusCode + "]: " + responseBody);
            }
            
        } catch (IOException e) {
            log.error("执行{}请求失败: {}", method, url, e);
            throw new ServiceException("执行请求失败: " + e.getMessage());
        }
    }

    /**
     * 构建完整URL
     *
     * @param endpoint API端点
     * @return 完整URL
     */
    private String buildUrl(String endpoint) {
        // 确保endpoint以/开头
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return String.format("%s://%s:%d%s", scheme, host, port, endpoint);
    }

    /**
     * 添加认证头
     *
     * @param request HTTP请求
     */
    private void addAuthHeader(HttpRequestBase request) {
        if (username != null && !username.isEmpty() && password != null) {
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            request.setHeader("Authorization", "Basic " + encodedAuth);
        }
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.error("关闭HTTP客户端失败", e);
        }
    }

    /**
     * 示例使用方法
     */
    public static void main(String[] args) {
        // 创建ES客户端
        ElasticsearchCurlClient client = new ElasticsearchCurlClient(
                "10.18.121.35", 9200, "http", "elastic", "123456"
        );
        
        try {
            // 类似 curl -X GET "localhost:9200/"
            String rootInfo = client.get("/");
            System.out.println("集群信息: " + rootInfo);
            
            // 类似 curl -X GET "localhost:9200/_cluster/health"
            String health = client.getClusterHealth();
            System.out.println("集群健康: " + health);
            
            // 类似 curl -X GET "localhost:9200/_cat/indices?v"
            String indices = client.getIndices();
            System.out.println("索引列表: " + indices);
            
            // 类似 curl -X POST "localhost:9200/test_index/_search" -H "Content-Type: application/json" -d '{"query":{"match_all":{}}}'
            String searchResult = client.search("test_index", "{\"query\":{\"match_all\":{}}}");
            System.out.println("搜索结果: " + searchResult);
            
        } finally {
            client.close();
        }
    }
}
