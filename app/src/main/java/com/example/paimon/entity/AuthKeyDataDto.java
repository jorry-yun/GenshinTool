package com.example.paimon.entity;

public class AuthKeyDataDto {
    private AuthKeyData data;
    private String message;
    private Integer retcode;

    public AuthKeyData getData() {
        return data;
    }

    public void setData(AuthKeyData data) {
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

    public static class AuthKeyData {
        private String authkey;
        private Integer authkey_ver;
        private Integer sign_type;

        public String getAuthkey() {
            return authkey;
        }

        public void setAuthkey(String authkey) {
            this.authkey = authkey;
        }

        public Integer getAuthkey_ver() {
            return authkey_ver;
        }

        public void setAuthkey_ver(Integer authkey_ver) {
            this.authkey_ver = authkey_ver;
        }

        public Integer getSign_type() {
            return sign_type;
        }

        public void setSign_type(Integer sign_type) {
            this.sign_type = sign_type;
        }
    }
}
