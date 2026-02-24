package com.chen.common.exception;

import com.chen.common.utils.StringUtils;
import com.chen.common.utils.resultreturn.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author chenyunzhi
 * @DATE 2024/7/22 15:40
 * @Description: 全局异常处理
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public ResultData<Object> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? ResultData.error(code, e.getMessage()) : ResultData.error(500,e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResultData<Object> handleException(Exception e, HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestUrl, e);
        return ResultData.error(String.format("请求地址'%s',发生未知异常. %s", requestUrl,e.getMessage()));
    }

}
