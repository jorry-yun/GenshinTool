package com.example.paimon.entity;

public class AuthKeyPostData {

    private String auth_appid;
    private String game_biz;
    private String game_uid;
    private String region;

    public AuthKeyPostData(String auth_appid, String game_biz, String game_uid, String region) {
        this.auth_appid = auth_appid;
        this.game_biz = game_biz;
        this.game_uid = game_uid;
        this.region = region;
    }

    public String getAuth_appid() {
        return auth_appid;
    }

    public void setAuth_appid(String auth_appid) {
        this.auth_appid = auth_appid;
    }

    public String getGame_biz() {
        return game_biz;
    }

    public void setGame_biz(String game_biz) {
        this.game_biz = game_biz;
    }

    public String getGame_uid() {
        return game_uid;
    }

    public void setGame_uid(String game_uid) {
        this.game_uid = game_uid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
