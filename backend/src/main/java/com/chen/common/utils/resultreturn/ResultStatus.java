package com.chen.common.utils.resultreturn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author: 神的孩子都在歌唱
 * @date: 2023/9/20 16:34
 * @Description:
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ResultStatus {

    /**
     * 成功状态码
     */
    public static final ResultStatus SUCCESS = ResultStatus.success(200, "操作成功");

    /**
     * 错误状态码
     */
    public static final ResultStatus ERROR = ResultStatus.error(500, "操作失败");


    /**
     * 未授权状态码
     */
    public static final ResultStatus FORBIDDEN = ResultStatus.error(403, "无访问权限");

    /**
     * 不接受请求中的内容要求
     */
    public static final ResultStatus HTTP_NOT_ACCEPTABLE = ResultStatus.error(406, "不接受请求中的内容要求");

    /**
     * 第一次登录
     */
    public static final ResultStatus FIRST_LOGIN = ResultStatus.error(502, "第一次登录要修改密码");

    /**
     * 第一次登录,判断姓名/邮箱/电话是否为空
     */
    public static final ResultStatus FIRST_LOGIN_VALIDATE = ResultStatus.error(503, "姓名/邮箱/电话不能为空");

    /**
     * 数据正在恢复，请稍后重试
     */
    public static final ResultStatus SYSYTEM_RECOVER = ResultStatus.error(500, "数据正在恢复，请稍后重试");

    /**
     * 无法连接服务
     */
    public static final ResultStatus GENERAL_ERROE = ResultStatus.error(500, "无法连接服务");
    /**
     * 登录: 未设置发件服务器
     */
    public static final ResultStatus EMAIL_SFTP_SET = ResultStatus.error(600, "当前未设置发件服务器，请前往设置");

    /**
     * 未登录状态码
     */
    public static final ResultStatus UNAUTHORIZED = ResultStatus.error(602, "登录信息已过期,请重新登录");

    /**
     * 登录密码已过期
     */
    public static final ResultStatus LOGIN_EXPIRE = ResultStatus.error(601, "登录密码已过期,请重新登录");
    /**
     * 激活码过期或未激活
     */
    public static final ResultStatus  AUTHRIZATION_ACTIVITI= ResultStatus.error(603, "未激活");

    /**
     * 设备重启/关闭 引擎重启
     */
    public static final ResultStatus MAINTAIN = ResultStatus.error(606, "设备维护中");

    /**
     * 系统正在升级
     */
    public static final ResultStatus SYSYTEM_UPDATE = ResultStatus.error(607, "系统正在升级中，请稍后再试");

    /**
     * ntp未检测到有效地址，服务开启失败！
     */
    public static final ResultStatus NTP_WARN = ResultStatus.error(608, "未检测到有效地址，服务开启失败！");

    private int code;
    private String message;

    private boolean result;
    private boolean needMerge;


    public ResultStatus() {
    }

    public ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static ResultStatus success(int code, String message) {
        return new ResultStatus(code, message);
    }

    public static ResultStatus success(int code) {
        return new ResultStatus(code, SUCCESS.message);
    }

    public static ResultStatus success(String message) {
        return new ResultStatus(SUCCESS.code, message);
    }

    public static ResultStatus error(int code, String message) {
        return new ResultStatus(code, message);
    }

    public static ResultStatus error(int code) {
        return new ResultStatus(code, ERROR.message);
    }

    public static ResultStatus error(String message) {
        return new ResultStatus(ERROR.code, message);
    }
}
