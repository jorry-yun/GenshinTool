package com.example.paimon.entity;


public class LoginCookieDataDto {

    private Integer code;
    private Data data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private AccountInfo account_info;
        private String game_ctrl_info;
        private String msg;
        private Integer status;

        public AccountInfo getAccount_info() {
            return account_info;
        }

        public void setAccount_info(AccountInfo account_info) {
            this.account_info = account_info;
        }

        public String getGame_ctrl_info() {
            return game_ctrl_info;
        }

        public void setGame_ctrl_info(String game_ctrl_info) {
            this.game_ctrl_info = game_ctrl_info;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    public static class AccountInfo {
        private Integer account_id;
        private Long create_time;
        private String email;
        private String identity_code;
        private Integer is_adult;
        private String is_email_verify;
        private String real_name;
        private String safe_area_code;
        private Integer safe_level;
        private String safe_mobile;
        private String weblogin_token;

        public Integer getAccount_id() {
            return account_id;
        }

        public void setAccount_id(Integer account_id) {
            this.account_id = account_id;
        }

        public Long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(Long create_time) {
            this.create_time = create_time;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getIdentity_code() {
            return identity_code;
        }

        public void setIdentity_code(String identity_code) {
            this.identity_code = identity_code;
        }

        public Integer getIs_adult() {
            return is_adult;
        }

        public void setIs_adult(Integer is_adult) {
            this.is_adult = is_adult;
        }

        public String getIs_email_verify() {
            return is_email_verify;
        }

        public void setIs_email_verify(String is_email_verify) {
            this.is_email_verify = is_email_verify;
        }

        public String getReal_name() {
            return real_name;
        }

        public void setReal_name(String real_name) {
            this.real_name = real_name;
        }

        public String getSafe_area_code() {
            return safe_area_code;
        }

        public void setSafe_area_code(String safe_area_code) {
            this.safe_area_code = safe_area_code;
        }

        public Integer getSafe_level() {
            return safe_level;
        }

        public void setSafe_level(Integer safe_level) {
            this.safe_level = safe_level;
        }

        public String getSafe_mobile() {
            return safe_mobile;
        }

        public void setSafe_mobile(String safe_mobile) {
            this.safe_mobile = safe_mobile;
        }

        public String getWeblogin_token() {
            return weblogin_token;
        }

        public void setWeblogin_token(String weblogin_token) {
            this.weblogin_token = weblogin_token;
        }
    }
}
