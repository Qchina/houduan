package org.csu.petstore.common;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 统一API响应结构
 */
@Data
public class ApiResponse {
    /** 是否成功 */
    private boolean success;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private Object data;
    /** 响应时间 */
    private LocalDateTime timestamp;
    /** 状态码 */
    private int code;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = success ? 200 : 500;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message) {
        this(success, message, null);
    }

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(false, message);
    }
    
    public static ApiResponse fail(String message, Object data) {
        return new ApiResponse(false, message, data);
    }
} 