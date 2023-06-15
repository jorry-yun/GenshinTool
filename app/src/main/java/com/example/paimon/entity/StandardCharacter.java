package com.example.paimon.entity;

import android.content.Context;

import com.example.paimon.CommUtil;
import com.example.paimon.R;
import com.example.paimon.util.CharacterStyle;
import com.example.paimon.util.DateUtils;
import com.example.paimon.util.GsonUtil;
import com.example.paimon.util.HttpCallBack;
import com.example.paimon.util.HttpUtil;
import com.example.paimon.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardCharacter {

    private static final Map<String, String> map = new HashMap<>();

    static {
        StandardCharacter.map.put("迪卢克", null);
        StandardCharacter.map.put("刻晴", "2021-02-17 18:00:00到2021-03-02 18:00:00");
        StandardCharacter.map.put("莫娜", null);
        StandardCharacter.map.put("琴", null);
        StandardCharacter.map.put("七七", null);
        StandardCharacter.map.put("提纳里", "2022-08-24 00:00:00到2022-09-09 18:00:00");
        StandardCharacter.map.put("迪希雅", "2023-03-01 00:00:00到2023-03-21 18:00:00");
    }

    private String name;
    private Date start;

    public StandardCharacter(String name) {
        this.name = name;
    }

    public StandardCharacter(String name, Date start, Date end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    private Date end;

    public static void pullConfig(Context context) {
        String url = context.getResources().getString(R.string.url_standard_character);
        new HttpUtil().get(url, Map.class, new HttpCallBack<Map>() {
            @Override
            public void onSuccess(Map map) {
                StandardCharacter.map.putAll(map);
                String content = GsonUtil.toJson(StandardCharacter.map);
                CommUtil.getInstance().writeCacheFile(context, content, "standard-character.json");
            }

            @Override
            public void onFailure(String message) {
                Log.d(message);
            }
        });
        String content = CommUtil.getInstance().readCacheFile(context, "standard-character.json", "{}");
        StandardCharacter.map.putAll(GsonUtil.parseJson(content, Map.class));
    }

    public static List<StandardCharacter> getList() {
        List<StandardCharacter> standardCharacterList = new ArrayList<>();
        for (Map.Entry<String, String> entry : StandardCharacter.map.entrySet()) {
            String date = entry.getValue();
            if (date != null && !"".equals(date)) {
                Date start = DateUtils.universalParseDate(date.split("到")[0]);
                Date end = DateUtils.universalParseDate(date.split("到")[1]);
                standardCharacterList.add(new StandardCharacter(entry.getKey(), start, end));
            } else {
                standardCharacterList.add(new StandardCharacter(entry.getKey()));
            }
        }
        return standardCharacterList;
    }
}
