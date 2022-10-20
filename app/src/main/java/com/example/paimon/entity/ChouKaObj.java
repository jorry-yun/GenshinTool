package com.example.paimon.entity;

import java.util.List;

public class ChouKaObj {

    private Integer code;
    private String msg;
    private List<ListUrl> urlListObj;

    public ChouKaObj(Integer code, String msg, List<ListUrl> urlListObj) {
        this.code = code;
        this.msg = msg;
        this.urlListObj = urlListObj;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<ListUrl> getUrlListObj() {
        return urlListObj;
    }

    public void setUrlListObj(List<ListUrl> urlListObj) {
        this.urlListObj = urlListObj;
    }
}
