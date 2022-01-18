package com.example.paimon.util;

public interface HttpCallBack<T>{
    void onSuccess(T t);
    void onFailure(String message);
}