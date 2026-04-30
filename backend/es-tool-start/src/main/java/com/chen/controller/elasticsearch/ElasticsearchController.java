package com.chen.controller.elasticsearch;
import com.chen.common.config.NavigateConfig;
import com.chen.common.utils.BeanUtils;
import com.chen.common.utils.json.FastJsonUtils;
import com.chen.common.utils.json.ReadJsonUtils;
import com.chen.common.utils.resultreturn.ResultData;
import com.chen.common.utils.resultreturn.ResultStatus;
import com.chen.controller.elasticsearch.domin.OperationCommand;
import com.chen.domain.elsaticsearch.ElasticsearchConnectParam;
import com.chen.domain.elsaticsearch.ElasticsearchFactoryParam;
import com.chen.domain.elsaticsearch.ElasticsearchHttpRequestParam;
import com.chen.service.ElasticsearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

    /**
     * es的http请求操作
     */
    @ApiOperation("es的http请求操作")
    @PostMapping("/httpOperation")
    public ResultData<Object> httpOperation(@RequestBody ElasticsearchHttpRequestParam operationCommand) throws IOException {

        return ResultData.success(elasticsearchService.httpOperation(
                operationCommand));
    }

    @ApiOperation("新增ES连接配置")
    @PostMapping("/connectParam")
    public ResultData<Void> addConnectParam(@RequestBody ElasticsearchConnectParam param) {
        String path = NavigateConfig.getEsConnectParamPath();
        String json = ReadJsonUtils.readJsonFile(path);
        List<ElasticsearchConnectParam> list = FastJsonUtils.toList(json, ElasticsearchConnectParam.class);
        boolean exists = list.stream()
                .anyMatch(p -> Objects.equals(p.getHostName(), param.getHostName())
                        && Objects.equals(p.getPort(), param.getPort()));
        if (exists) {
            return ResultData.error(ResultStatus.error("该连接已存在"));
        }
        list.add(param);
        ReadJsonUtils.writeJsonFile(path, FastJsonUtils.toJSONString(list));
        return ResultData.success();
    }

    @ApiOperation("删除ES连接配置")
    @DeleteMapping("/connectParam")
    public ResultData<Void> deleteConnectParam(@RequestBody ElasticsearchConnectParam param) {
        String path = NavigateConfig.getEsConnectParamPath();
        String json = ReadJsonUtils.readJsonFile(path);
        List<ElasticsearchConnectParam> list = FastJsonUtils.toList(json, ElasticsearchConnectParam.class);
        boolean removed = list.removeIf(p -> Objects.equals(p.getHostName(), param.getHostName())
                && Objects.equals(p.getPort(), param.getPort()));
        if (!removed) {
            return ResultData.error(ResultStatus.error("连接不存在"));
        }
        ReadJsonUtils.writeJsonFile(path, FastJsonUtils.toJSONString(list));
        return ResultData.success();
    }

}
