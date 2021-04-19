package com.offcn.entity;

import java.io.Serializable;

/**
 * @Auther: lhq
 * @Date: 2021/3/10 11:15
 * @Description:  更新数据返回结果的封装类
 */
public class Result implements Serializable {

    private boolean success;   //更新成功的标识
    private String message; //更新话术

    public Result(){

    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
