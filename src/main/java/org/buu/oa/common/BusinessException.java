package org.buu.oa.common;

/**
 * 业务异常类
 * 用于封装业务逻辑中出现的异常情况，可携带自定义状态码
 */
public class BusinessException extends RuntimeException {

    /** 错误状态码 */
    private Integer code;

    /**
     * 创建业务异常（默认500）
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 创建业务异常（自定义状态码）
     * @param code 状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误状态码
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }
}