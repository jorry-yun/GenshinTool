package com.example.paimon.entity;

import java.util.List;

public class UserGameRolesByCookieDataDto {

    private UserGameRolesByCookieData data;
    private String message;
    private Integer retcode;

    public UserGameRolesByCookieData getData() {
        return data;
    }

    public void setData(UserGameRolesByCookieData data) {
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

    public static class UserGameRolesByCookieData {
        private List<UserService> list;

        public List<UserService> getList() {
            return list;
        }

        public void setList(List<UserService> list) {
            this.list = list;
        }
    }

    public static class UserService {
        private String game_biz;
        private String game_uid;
        private Boolean is_chosen;
        private Boolean is_official;
        private Integer level;
        private String nickname;
        private String region;
        private String region_nam;

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

        public Boolean getIs_chosen() {
            return is_chosen;
        }

        public void setIs_chosen(Boolean is_chosen) {
            this.is_chosen = is_chosen;
        }

        public Boolean getIs_official() {
            return is_official;
        }

        public void setIs_official(Boolean is_official) {
            this.is_official = is_official;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRegion_nam() {
            return region_nam;
        }

        public void setRegion_nam(String region_nam) {
            this.region_nam = region_nam;
        }
    }
}
