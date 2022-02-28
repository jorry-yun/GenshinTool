package com.example.paimon.util;

import android.content.Context;

import com.example.paimon.CommUtil;
import com.example.paimon.MainActivity;
import com.example.paimon.R;

import java.util.HashMap;
import java.util.Map;

public class CharacterStyle {

    private static final Map<String, Integer> map = new HashMap<>();

    static {
        map.put("神里绫人", R.color.shui);
        map.put("八重神子", R.color.lei);
        map.put("申鹤", R.color.bing);
        map.put("云堇", R.color.yan);
        map.put("荒泷一斗", R.color.yan);
        map.put("五郎", R.color.yan);
        map.put("优菈", R.color.bing);
        map.put("阿贝多", R.color.yan);
        map.put("胡桃", R.color.huo);
        map.put("达达利亚", R.color.shui);
        map.put("埃洛伊", R.color.bing);
        map.put("珊瑚宫心海", R.color.shui);
        map.put("雷电将军", R.color.lei);
        map.put("宵宫", R.color.huo);
        map.put("神里绫华", R.color.bing);
        map.put("枫原万叶", R.color.feng);
        map.put("钟离", R.color.yan);
        map.put("魈", R.color.feng);
        map.put("温迪", R.color.feng);
        map.put("甘雨", R.color.bing);
        map.put("七七", R.color.bing);
        map.put("刻晴", R.color.lei);
        map.put("可莉", R.color.huo);
        map.put("迪卢克", R.color.huo);
        map.put("莫娜", R.color.shui);
        map.put("琴", R.color.feng);
        map.put("托马", R.color.huo);
        map.put("九条裟罗", R.color.lei);
        map.put("罗莎莉亚", R.color.bing);
        map.put("早柚", R.color.feng);
        map.put("雷泽", R.color.lei);
        map.put("凝光", R.color.yan);
        map.put("菲谢尔", R.color.lei);
        map.put("班尼特", R.color.huo);
        map.put("烟绯", R.color.huo);
        map.put("重云", R.color.bing);
        map.put("芭芭拉", R.color.shui);
        map.put("迪奥娜", R.color.bing);
        map.put("砂糖", R.color.feng);
        map.put("诺艾尔", R.color.yan);
        map.put("凯亚", R.color.bing);
        map.put("辛焱", R.color.huo);
        map.put("香菱", R.color.huo);
        map.put("北斗", R.color.lei);
        map.put("行秋", R.color.shui);
        map.put("安柏", R.color.huo);
        map.put("丽莎", R.color.lei);
    }

    public static void pullConfig(Context context) {
        String url = "https://files.cnblogs.com/files/blogs/682374/character-style.json";
        new HttpUtil().get(url, Map.class, new HttpCallBack<Map>() {
            @Override
            public void onSuccess(Map map) {
                CharacterStyle.map.putAll(convertMap(map));
                String content = GsonUtil.toJson(CharacterStyle.map);
                CommUtil.getInstance().writeCacheFile(context, content, "character-style.json");
            }

            @Override
            public void onFailure(String message) {
                Log.d(message);
            }
        });
        String content = CommUtil.getInstance().readCacheFile(context, "character-style.json", "{}");
        map.putAll(convertMap(GsonUtil.parseJson(content, Map.class)));
    }

    public static Integer get(String name) {
        return map.get(name);
    }

    private static Integer getColor(String name) {
        switch (name) {
            case "风": return R.color.feng;
            case "岩": return R.color.yan;
            case "冰": return R.color.bing;
            case "火": return R.color.huo;
            case "水": return R.color.shui;
            case "雷": return R.color.lei;
        }
        return null;
    }

    private static Map<String, Integer> convertMap(Map map) {
        Map<String, Object> request = new HashMap<>(map);
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : request.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer || value instanceof Double) {
                result.put(entry.getKey(), ((Number) value).intValue());
            } else {
                result.put(entry.getKey(), getColor((String) entry.getValue()));
            }
        }
        return result;
    }
}
