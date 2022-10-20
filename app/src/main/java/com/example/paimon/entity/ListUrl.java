package com.example.paimon.entity;

public class ListUrl {

    private String uid;
    private String url;

    public ListUrl(String uid, String url) {
        this.uid = uid;
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
