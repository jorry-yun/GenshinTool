package com.example.paimon.entity;

import java.util.List;

public class AppUpdateMsg {

    private List<UpdateHistory> data;

    public List<UpdateHistory> getData() {
        return data;
    }

    public void setData(List<UpdateHistory> data) {
        this.data = data;
    }

    public class UpdateHistory {
        private String version;
        private String apkName;
        private String size;
        private String url;
        private String date;
        private List<String> updateInfo;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getApkName() {
            return apkName;
        }

        public void setApkName(String apkName) {
            this.apkName = apkName;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<String> getUpdateInfo() {
            return updateInfo;
        }

        public void setUpdateInfo(List<String> updateInfo) {
            this.updateInfo = updateInfo;
        }
    }

}
