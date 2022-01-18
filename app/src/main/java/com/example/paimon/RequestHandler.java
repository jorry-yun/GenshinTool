package com.example.paimon;

import com.example.paimon.entity.WishVo;

import java.util.List;

public interface RequestHandler {

    void onComplete(List<WishVo> list);
}
