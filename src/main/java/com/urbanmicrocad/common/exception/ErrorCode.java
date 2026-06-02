package com.urbanmicrocad.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "参数错误"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "未登录或登录已过期"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "无权限访问该资源"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "资源不存在"),
    CONFLICT(HttpStatus.CONFLICT, "资源状态冲突"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
