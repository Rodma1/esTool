package com.chen.domain.elsaticsearch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * es文档
 *
 * @author chenyunzhi
 * @date 2024/7/15 21:08
 */

@Data
@Accessors(chain = true)
public class ElasticsearchDocument {

    @ApiModelProperty("索引名")
    public String index;

    @ApiModelProperty("文档id")
    public String id;

    @ApiModelProperty("文档")
    public HashMap source;
}
