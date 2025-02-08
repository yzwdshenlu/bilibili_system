package com.daVinci.bilibili.domain;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.domain
 * @Author: daVinci
 * @CreateTime: 2025-01-20  21:03
 * @Description: Json返回数据类
 * @Version: 1.0
 */
public class JsonResponse<T> {
    private String code;
    private String msg;
    private T data;

    public JsonResponse(String code,String msg){
        this.code = code;
        this.msg = msg;
    }
    public JsonResponse(T data){
        this.data = data;
        this.msg = "成功";
        this.code = "0";
    }

    /**
     * 成功，但不返回数据
     * @return
     */
    public static JsonResponse<String> success(){
        return new JsonResponse<>(null);
    }

    /**
     * 成功，且返回数据
     * @param data
     * @return
     */
    public static JsonResponse<String> success(String data){
        return new JsonResponse<>(data);
    }

    /**
     * 返回通用的失败信息
     * @return
     */
    public static JsonResponse<String> fail(){
        return new JsonResponse<>("1","失败");
    }

    /**
     * 返回特别的失败信息
     * @param code
     * @param msg
     * @return
     */
    public static JsonResponse<String> fail(String code,String msg){
        return new JsonResponse<>(code,msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
