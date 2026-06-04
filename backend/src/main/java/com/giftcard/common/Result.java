package com.giftcard.common;

public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    private String lang;
    
    // Getters
    public Integer getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getLang() { return lang; }
    
    // Setters
    public void setCode(Integer code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
    public void setLang(String lang) { this.lang = lang; }
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.setCode(404);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }
}
