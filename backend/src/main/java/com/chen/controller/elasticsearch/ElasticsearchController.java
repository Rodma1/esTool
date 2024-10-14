package com.chen.controller.elasticsearch;
import com.chen.common.config.NavigateConfig;
import com.chen.common.utils.BeanUtils;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.common.utils.json.ReadJsonUtils;
import com.chen.common.utils.resultreturn.ResultData;
import com.chen.controller.elasticsearch.domin.OperationCommand;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.service.elasticsearch.ElasticsearchService;
import com.chen.service.elasticsearch.impl.ElasticsearchOperationStrategy;
import com.chen.service.elasticsearch.operation.ElasticsearchOperationStrategyFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/2 17:59
 * @Description:
 */
@RequiredArgsConstructor
@RestController
@Api(value = "接口控制器", tags = "elasticsearch接口")
@RequestMapping("/elasticsearch")
public class ElasticsearchController {


    private final ElasticsearchService elasticsearchService;
    /**
     * 基本的操作接口
     */
    @ApiOperation("es基本的操作接口")
    @PostMapping("/operation")
    public ResultData<Object> performOperation(@RequestBody OperationCommand operationCommand) throws IOException {
        return ResultData.success(elasticsearchService.performOperation(
                BeanUtils.copyObject(operationCommand, ElasticsearchFactoryParam.class)
                ,BeanUtils.copyObject(operationCommand, ElasticsearchConnectParam.class)));
    }

    /**
     * 基本的操作接口
     */
    @ApiOperation("获取es连接详细")
    @GetMapping("/connectParam")
    public ResultData<List<ElasticsearchConnectParam>> getConnectParam() {
        String jsonObject = ReadJsonUtils.readJsonFile(NavigateConfig.getEsConnectParamPath());
        return ResultData.success(FastJsonUtils.toList(jsonObject, ElasticsearchConnectParam.class));
    }

}
