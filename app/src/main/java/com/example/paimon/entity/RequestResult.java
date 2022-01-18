package com.example.paimon.entity;

import java.io.Serializable;
import java.util.List;

public class RequestResult implements Serializable {

    private int retcode;//0为成功
    private String message;
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * 是否正常相应
     * @return  true or false
     */
    public boolean isOk() {
        return 0 == this.retcode;
    }

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public class Data {
        private List<WishVo> list;
        private String page;
        private String region;
        private String size;
        private String total;

        public List<WishVo> getList() {
            return list;
        }

        public void setList(List<WishVo> list) {
            this.list = list;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }
}