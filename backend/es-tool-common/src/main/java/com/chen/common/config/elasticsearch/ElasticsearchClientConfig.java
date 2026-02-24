package com.chen.common.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chenyunzhi
 */


@Slf4j
public class ElasticsearchClientConfig {

    private final String hostName;
    private final Integer port;
    private final String scheme;
    private final String userName;
    private final String password;
    //    使用 AtomicReference 来存储 ElasticsearchClient 实例，并且使用双重检查锁定（Double-checked locking）模式确保在多线程环境中安全地创建和访问单例实例。
    private final AtomicReference<ElasticsearchClient> clientReference = new AtomicReference<>();

    public ElasticsearchClientConfig(ElasticsearchConnectParam elastic) {
        this.userName = elastic.getUserName();
        this.password = elastic.getPassword();
        this.hostName = elastic.getHostName();
        this.port = elastic.getPort();
        this.scheme = elastic.getScheme();
        addShutdownHook();
    }

    /**
     * 获取单例的 ElasticsearchClient 实例
     *
     * @return ElasticsearchClient
     */
    public ElasticsearchClient elasticsearchClient() {
        ElasticsearchClient client = clientReference.get();
        if (client == null) {
            synchronized (this) {
                client = clientReference.get();
                if (client == null) {
                    client = createElasticsearchClient();
                    clientReference.set(client);
                }
            }
        }
        return client;
    }

    private ElasticsearchClient createElasticsearchClient() {
        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = getElasticsearchTransport(userName, password, 5000, 600000, new HttpHost(hostName, port, scheme));
        // create the API client
        return new ElasticsearchClient(transport);
    }

    private static ElasticsearchTransport getElasticsearchTransport(String username, String passwd, Integer connectionTimeout, Integer readTimeout, HttpHost... hosts) {
        // 官网地址： https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.4/_basic_authentication.html
        // 账号密码的配置
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, passwd));

        // 跳过证书校验
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] chain, String authType) {
                            return true; // Trust all certificates
                        }
                    }).build();
        } catch (Exception e) {
            throw new RuntimeException("Error setting up SSL context", e);
        }

        // 包含了账号密码
        RestClientBuilder.HttpClientConfigCallback callback = httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(10)
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                //  加上这个拦截器，补上 X-Elastic-Product 头
                .addInterceptorLast((HttpResponseInterceptor) (response, context) -> {
                    if (!response.containsHeader("X-Elastic-Product")) {
                        response.addHeader("X-Elastic-Product", "Elasticsearch");
                    }
                });

        RestClientBuilder builder = RestClient.builder(hosts)
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new org.apache.http.message.BasicHeader("Content-Type", "application/json")
                })
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(connectionTimeout)
                        .setSocketTimeout(readTimeout))
                .setHttpClientConfigCallback(callback);

        RestClient client = builder.build();

        return new RestClientTransport(client, new JacksonJsonpMapper());
    }

    //    提供 close 方法以允许在需要时显式关闭客户端。
    public void close() {
        ElasticsearchClient client = clientReference.get();
        if (client != null) {
            try {
                client._transport().close();
                clientReference.set(null);
            } catch (Exception e) {
                System.err.println("关闭失败: " + e.getMessage());
            }
        }
    }

    //    注册一个关闭钩子，在 JVM 关闭时自动调用 close 方法关闭 ElasticsearchClient 实例，释放资源。
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }


}
