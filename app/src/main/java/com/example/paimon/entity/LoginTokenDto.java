package com.example.paimon.entity;

import java.util.List;

public class LoginTokenDto {

    private LoginTokenData data;
    private String message;
    private Integer retcode;

    public LoginTokenData getData() {
        return data;
    }

    public void setData(LoginTokenData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRetcode() {
        return retcode;
    }

    public void setRetcode(Integer retcode) {
        this.retcode = retcode;
    }

    public static class LoginTokenData {
        private List<DataObj> list;

        public List<DataObj> getList() {
            return list;
        }

        public void setList(List<DataObj> list) {
            this.list = list;
        }
    }

    public static class DataObj {
        private String name;
        private String token;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
