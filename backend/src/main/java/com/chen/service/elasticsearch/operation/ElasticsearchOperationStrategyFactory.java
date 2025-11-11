package com.chen.service.elasticsearch.operation;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;

/**
 * @Author chenyunzhi
 * @DATE 2024/6/24 18:00
 * @Description: es 工厂模式
 */
public class ElasticsearchOperationStrategyFactory {

    /**
     * 创建策略
     */
    public static ElasticsearchOperationStrategy createStrategy(ElasticsearchFactoryParam factoryParam) {
        switch (factoryParam.getOperationCategory()) {
            case "INFO":
                return new VersionInfoOperationStrategy();
            case  "INDEX":
                return new IndexOperationStrategy(factoryParam);
            case "DOCUMENT":
                return new DocOperationStrategy(factoryParam);
            case "ALIAS":
                return new AliasesOperationStrategy(factoryParam);
            case "TEMPLATE":
                return new IndexTemplateOperationStrategy(factoryParam);
            case "TASK":
                return new TaskOperationStrategy(factoryParam);
            case "ANALYZE":
                return new AnalyzeOperationStrategy(factoryParam);
            case "OTHER":
                return new EsOther();
            default:
                return null;
        }
    }

}
