package com.chen.domain.elsaticsearch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/5 13:44
 * @Description:
 */
@Data
public class CatIndicesRecord {

    @ApiModelProperty("健康状态")
    public  String health;

    @ApiModelProperty("索引的状态")
    public  String status;

    @ApiModelProperty("索引的名称")
    public  String index;

    @ApiModelProperty("索引的唯一标识符")
    public  String uuid;

    @ApiModelProperty("主分片的数量")
    public  String pri;

    @ApiModelProperty("副本分片的数量")
    public  String rep;

    @ApiModelProperty("文档的数量")
    public  String docsCount;

    @ApiModelProperty("被删除但尚未清除的文档数量")
    public  String docsDeleted;

    @ApiModelProperty("索引所占用的存储空间")
    public String storeSize;

    @ApiModelProperty("主分片所占用的存储空间")
    public String priStoreSize;
}
