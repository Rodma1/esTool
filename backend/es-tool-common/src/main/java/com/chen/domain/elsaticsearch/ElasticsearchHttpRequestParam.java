package com.chen.domain.elsaticsearch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElasticsearchHttpRequestParam extends ElasticsearchConnectParam {

    @ApiModelProperty("Http方法 GET  POST PUT DELETE")
    private String method;

    @ApiModelProperty("API端点 列：/_cluster/health")
    private String endpoint;

    @ApiModelProperty("请求体 json")
    private String body;
}
