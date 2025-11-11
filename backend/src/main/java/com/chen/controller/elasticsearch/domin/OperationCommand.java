package com.chen.controller.elasticsearch.domin;

import com.chen.domain.elsaticsearch.ElasticsearchTimeSearch;
import com.chen.domain.elsaticsearch.SearchFields;
import com.chen.domain.elsaticsearch.UpdateFields;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/2 18:02
 * @Description:
 */
@ApiModel("基本操作的请求参数")
@Data
public class OperationCommand {

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("ip地址")
    private String hostName;

    @ApiModelProperty("端口")
    private Integer port;

    @ApiModelProperty("协议 http  https")
    private String scheme;


    @ApiModelProperty("操作类别 文档  索引")
    public String operationCategory;

    @ApiModelProperty("操作类型 添加 删除 修改等等")
    public String operationType;

    @ApiModelProperty("索引名")
    public String indexName;

    @ApiModelProperty("索引列表")
    public List<String> indices;

    @ApiModelProperty("文档")
    public String document;

    @ApiModelProperty("文档Id")
    public String documentId;

    @ApiModelProperty("文档Id列表")
    public List<String> documentIds;

    @ApiModelProperty("索引别名")
    public String alias;

    @ApiModelProperty("索引别名的写入索引")
    public String aliasWriteIndex;

    @ApiModelProperty("页数")
    public int pageNum;

    @ApiModelProperty("页数大小")
    public int pageSize;

    @ApiModelProperty("要排序的字段")
    public String sortField;

    @ApiModelProperty("排序 Asc 升序  Desc降序")
    public String sortOrder;


    @ApiModelProperty("索引模板名")
    public String indexTemplate;

    @ApiModelProperty("任务Id")
    public String taskId;

    @ApiModelProperty("索引模板内容")
    public String indexTemplateContent;

    @ApiModelProperty("查询字段")
    public List<SearchFields> searchFields;

    @ApiModelProperty("时间查询")
    public ElasticsearchTimeSearch timeSearch;

    @ApiModelProperty("更新字段")
    public List<UpdateFields> updateFields;

    @ApiModelProperty("分析器")
    public String analyzer;

    @ApiModelProperty("字段")
    public String field;
}
