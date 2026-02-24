package com.chen.service;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.config.elasticsearch.ElasticsearchClientConfig;
import com.chen.common.utils.StringUtils;
import com.chen.config.ElasticsearchClient7Config;
import com.chen.config.ElasticsearchClient8Config;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import com.chen.service.operation7.ElasticsearchOperation7StrategyFactory;
import com.chen.service.operation8.ElasticsearchOperation8StrategyFactory;
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

    private ElasticsearchClientConfig clientConfig;

    public Object performOperation(ElasticsearchFactoryParam factoryParam, ElasticsearchConnectParam connectParam) throws IOException {

        ElasticsearchOperationStrategy strategy = null;
        String version = connectParam.getVersion();
        if (StringUtils.isBlank(version)) {
            this.clientConfig = new ElasticsearchClient7Config(connectParam);
            strategy = ElasticsearchOperation7StrategyFactory.createStrategy(factoryParam);
        } else if (version.equals("8")) {
            this.clientConfig = new ElasticsearchClient8Config(connectParam);
            strategy = ElasticsearchOperation8StrategyFactory.createStrategy(factoryParam);
        } else if (version.equals("7")) {
            this.clientConfig = new ElasticsearchClient7Config(connectParam);
            strategy = ElasticsearchOperation7StrategyFactory.createStrategy(factoryParam);
        }
        else {
            this.clientConfig = new ElasticsearchClient7Config(connectParam);
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
            // 不在此处关闭客户端，交给 Spring 容器或 JVM 关闭钩子处理
        }
    }

    /**
     * 确保资源在应用关闭时得到释放
     */

    @Override
    public void destroy(){
        System.out.println("ElasticsearchService is being destroyed.");
        clientConfig.close();
    }
}
