package com.chen.common.utils.resultreturn;

/**
 * @author: 神的孩子都在歌唱
 * @date: 2023/9/20 16:34
 * @Description:
 */
public class ResultData<T> {

    /**
     * 响应码
     */
    private int code;

    /**
     * 返回结果描述
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResultData() {
    }

    public ResultData(ResultStatus resultStatus) {
        this.code = resultStatus.code();
        this.message = resultStatus.message();
    }

    public ResultData(ResultStatus resultStatus, T data) {
        this.code = resultStatus.code();
        this.message = resultStatus.message();
        this.data = data;
    }

    public ResultData<T> code(int value) {
        this.code = value;
        return this;
    }

    public ResultData<T> message(String value) {
        this.message = value;
        return this;
    }

    public ResultData<T> data(T value) {
        this.data = value;
        return this;
    }

    public static <T> ResultData<T> success() {
        return new ResultData<T>(ResultStatus.SUCCESS);
    }

//    public static <T> ResultData<T> success(int code) {
//        return new ResultData<T>(ResultStatus.success(code));
//    }

//    public static <T> ResultData<T> success(String message) {
//        return new ResultData<T>(ResultStatus.success(message));
//    }

    public static <T> ResultData<T> success(int code, String message) {
        return new ResultData<T>(ResultStatus.success(code, message));
    }

    public static <T> ResultData<T> success(T data) {
        return new ResultData<T>(ResultStatus.SUCCESS, data);
    }

    public static <T> ResultData<T> success(int code, T data) {
        return new ResultData<T>(ResultStatus.success(code), data);
    }

    public static <T> ResultData<T> success(String message, T data) {
        return new ResultData<T>(ResultStatus.success(message), data);
    }

    public static <T> ResultData<T> success(int code, String message, T data) {
        return new ResultData<T>(ResultStatus.success(code, message), data);
    }

    public static <T> ResultData<T> success(ResultStatus resultStatus) {
        return new ResultData<T>(resultStatus);
    }

    public static <T> ResultData<T> success(ResultStatus resultStatus, T data) {
        return new ResultData<T>(resultStatus, data);
    }

    public static <T> ResultData<T> error() {
        return new ResultData<T>(ResultStatus.ERROR);
    }

    public static <T> ResultData<T> error(T data) {
        return new ResultData<T>(ResultStatus.ERROR, data);
    }

//    public static <T> ResultData<T> error(int code) {
//        return new ResultData<T>(ResultStatus.error(code));
//    }

//    public static <T> ResultData<T> error(String message) {
//        return new ResultData<T>(ResultStatus.error(message));
//    }

    public static <T> ResultData<T> error(int code, String message) {
        return new ResultData<T>(ResultStatus.error(code, message));
    }

    public static <T> ResultData<T> error(ResultStatus resultStatus) {
        return new ResultData<T>(resultStatus);
    }
}
