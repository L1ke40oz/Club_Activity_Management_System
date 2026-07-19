package com.example.clubmanagementsystem.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码：200成功，400失败等
    private String message; // 提示信息
    private T data; // 具体的数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}