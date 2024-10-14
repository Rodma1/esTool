package com.chen.service.elasticsearch.operation;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.chen.common.config.NavigateConfig;
import com.chen.common.exception.ServiceException;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.common.utils.json.ReadJsonUtils;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


/**
 * @Author chenyunzhi
 * @DATE 2024/9/30 10:50
 * @Description:
 */
@Slf4j
public class EsOther implements ElasticsearchOperationStrategy {
    @Override
    public Object execute(ElasticsearchClient elasticsearchClient) throws IOException {
        return this.connectParam();
    }

    public Object connectParam() {
        try {
//            String path = "/study/project/navigate/data/esConnectParam.json";
            log.info(NavigateConfig.getEsConnectParamPath());
            String jsonObject = ReadJsonUtils.readJsonFile(NavigateConfig.getEsConnectParamPath());
            return FastJsonUtils.toList(jsonObject, ElasticsearchConnectParam.class);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

}
