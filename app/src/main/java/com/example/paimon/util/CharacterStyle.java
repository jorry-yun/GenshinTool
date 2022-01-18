package com.example.paimon.util;

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

    public static Integer get(String name) {
        return map.get(name);
    }
}
