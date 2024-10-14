package com.chen.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.config.elasticsearch.ElasticsearchClientConfig;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import com.chen.service.elasticsearch.operation.ElasticsearchOperationStrategyFactory;
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
        this.clientConfig = new ElasticsearchClientConfig(connectParam);
        ElasticsearchClient client = clientConfig.elasticsearchClient();
        ElasticsearchOperationStrategy strategy = ElasticsearchOperationStrategyFactory.createStrategy(factoryParam);
        if (strategy != null) {
            Object execute = strategy.execute(client);
            return execute;
        }
        return null;
    }

    /**
     * 确保资源在应用关闭时得到释放
     */

    @Override
    public void destroy(){
        clientConfig.close();
    }
}
