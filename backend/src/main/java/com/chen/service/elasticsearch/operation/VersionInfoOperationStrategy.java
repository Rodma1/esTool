package com.chen.service.elasticsearch.operation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.exception.ServiceException;
import com.chen.common.utils.BeanUtils;
import com.chen.domain.elsaticsearch.ElasticsearchVersionInfo;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

import java.io.IOException;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/8 17:52
 * @Description:
 */
public class VersionInfoOperationStrategy implements ElasticsearchOperationStrategy {
    @Override
    public Object execute(ElasticsearchClient elasticsearchClient) throws IOException {
        try {
            return BeanUtils.copyObject(elasticsearchClient.info().version(), ElasticsearchVersionInfo.class);

        } catch (Exception e) {
            throw new ServiceException("链接失败: " + e.getMessage());
        }
    }
}
