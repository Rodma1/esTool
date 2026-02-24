package com.chen.domain.elsaticsearch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/16 17:57
 * @Description:
 */
@Data
public class CatAliasesRecord implements Serializable {

    @ApiModelProperty("别名")
    public String alias;

    @ApiModelProperty("索引名")
    public String index;

    @ApiModelProperty("")
    public String filter;

    @ApiModelProperty("")
    public String routingIndex;

    @ApiModelProperty("")
    public String routingSearch;

    @ApiModelProperty("是否是写入索引")
    public String isWriteIndex;

}
