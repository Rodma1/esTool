package com.chen.service;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.config.elasticsearch.ElasticsearchClientConfig;
import com.chen.common.utils.StringUtils;
import com.chen.config.ElasticsearchClient7Config;
import com.chen.config.ElasticsearchClient8Config;
import com.chen.config.ElasticsearchClient9Config;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.domain.elsaticsearch.ElasticsearchHttpRequestParam;
import com.chen.service.elasticsearch.ElasticsearchCurlClient;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import com.chen.service.operation7.ElasticsearchOperation7StrategyFactory;
import com.chen.service.operation8.ElasticsearchOperation9StrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author chenyunzhi
 * @Date 2024/6/24 17:02
 * @Description: 封装对es的操作
 */

@Service
@RequiredArgsConstructor
public class ElasticsearchService implements DisposableBean {

//    private ElasticsearchClientConfig clientConfig;

    public Object performOperation(ElasticsearchFactoryParam factoryParam, ElasticsearchConnectParam connectParam) throws IOException {
        if (ObjectUtil.isNull(connectParam.getPort())) {
            return null;
        }
        ElasticsearchClientConfig clientConfig = null;
        ElasticsearchOperationStrategy strategy = null;
        String version = connectParam.getVersion();
        if (StringUtils.isBlank(version)) {
            clientConfig = new ElasticsearchClient7Config(connectParam);
            strategy = ElasticsearchOperation7StrategyFactory.createStrategy(factoryParam);
        } else if (version.equals("8")) {
            clientConfig = new ElasticsearchClient8Config(connectParam);
            strategy = ElasticsearchOperation9StrategyFactory.createStrategy(factoryParam);
        } else if (version.equals("9")) {
            clientConfig = new ElasticsearchClient9Config(connectParam);
            strategy = ElasticsearchOperation9StrategyFactory.createStrategy(factoryParam);
        }

        else if (version.equals("7")) {
            clientConfig = new ElasticsearchClient7Config(connectParam);
            strategy = ElasticsearchOperation7StrategyFactory.createStrategy(factoryParam);
        }
        else {
            clientConfig = new ElasticsearchClient7Config(connectParam);
            strategy = ElasticsearchOperation7StrategyFactory.createStrategy(factoryParam);
        }

        ElasticsearchClient client = clientConfig.elasticsearchClient();
        // 使用 try-finally 块增强异常安全性，避免资源泄漏
        try {
            if (strategy != null) {
                return strategy.execute(client);
            }
            return null;
        } finally {
            // 只关闭当前请求的配置，不影响其他请求
            clientConfig.close();
            // 不在此处关闭客户端，交给 Spring 容器或 JVM 关闭钩子处理
        }
    }

    /**
     * 确保资源在应用关闭时得到释放
     */

    @Override
    public void destroy(){
        System.out.println("ElasticsearchService is being destroyed.");
//        clientConfig.close();
    }

    public Object httpOperation(ElasticsearchHttpRequestParam connectParam) {

        if (ObjectUtil.isNull(connectParam.getPort())) {
            return null;
        }
        ElasticsearchCurlClient client = new ElasticsearchCurlClient(
                connectParam.getHostName(), connectParam.getPort(), connectParam.getScheme(), connectParam.getUserName(), connectParam.getPassword()
        );
        try {
            return client.executeRequest(connectParam.getMethod(), connectParam.getEndpoint(), connectParam.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }
}
