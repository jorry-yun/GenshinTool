package com.example.paimon;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paimon.entity.RequestResult;
import com.example.paimon.entity.WishVo;
import com.example.paimon.util.CharacterStyle;
import com.example.paimon.util.GsonUtil;
import com.example.paimon.util.HttpCallBack;
import com.example.paimon.util.HttpUtil;
import com.example.paimon.util.Log;
import com.example.paimon.util.StringUtil;
import com.example.paimon.util.SystemUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String data = msg.getData().getString("data");
            Log.d(data);
            List<WishVo> wishList = GsonUtil.jsonToList(data, WishVo.class);
            switch (msg.what) {
                case 1: showCharacter(wishList); break;
                case 2: showStandard(wishList); break;
                case 3: showWeapon(wishList); break;
            }
        }
    };

    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                doWish(msg.getData().getString("data"), 1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText content = findViewById(R.id.content);
        // 读取缓存
        SharedPreferences paimon = getSharedPreferences("paimon", MODE_PRIVATE);
        String cacheUrl = paimon.getString("content", "");
        content.setText(cacheUrl);
        // 监听按钮点击事件
        TextView button = findViewById(R.id.button);
        button.setOnClickListener((view) -> {
            String url = content.getText().toString();
            if (url.length() != 0) {
                Map<String, String> resultMap = parseUrl(url);
                String authKey = resultMap.get("authkey");
                if (authKey != null && authKey.length() > 0) {
                    // 设置缓存
                    paimon.edit().putString("content", url).apply();
                    Toast.makeText(this, "正在获取祈愿池信息，时间可能有点长，请耐心等待", Toast.LENGTH_SHORT).show();
                    sendMessage(0, toUrl(resultMap) + "&gacha_type=", handler2);
                } else {
                    showDialog("输入的链接有误，请检查");
                }
            } else {
                showDialog("请输入抽卡记录链接！");
            }
        });

//        TextView moni = findViewById(R.id.moni);
//        moni.setOnClickListener((view) -> {
//            List<WishVo> data = new ArrayList<>();
//            int j = 0;
//            for (int i = 0; i < 150; i++) {
//                WishVo vo = new WishVo();
//                vo.setId("-1");
//                vo.setName("模拟数据");
//                vo.setUid("100049717");
//                j++;
//                if (j < 9) {
//                    if ((int)(Math.random() * 100) + 1 <= 5) {
//                        vo.setRank_type("4");
//                        j = 0;
//                    }
//                }
//                if (j == 9) {
//                    if ((int)(Math.random() * 100) + 1 <= 36) {
//                        vo.setRank_type("4");
//                        j = 0;
//                    }
//                }
//                if (i == 0 || j == 10) {
//                    vo.setRank_type("4");
//                    j = 0;
//                }
//                if (i == 74) {
//                    vo.setName("莫娜");
//                    vo.setRank_type("5");
//                }
//                data.add(vo);
//            }
//            paimon.edit().putString("100049717-301", GsonUtil.toJson(data)).apply();
//            Toast.makeText(this, "模拟数据生成成功", Toast.LENGTH_SHORT).show();
//        });

        // 页面跳转
        TextView tips = findViewById(R.id.tips);
        tips.setOnClickListener((view) -> {
            Intent tipsIntent = new Intent(this, TipActivity.class);
            startActivity(tipsIntent);
        });
    }

    private void sendMessage(int what, String data, Handler handler) {
        Message msg = new Message();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private Map<String, String> parseUrl(String url) {
        Map<String, String> result = new HashMap<>();
        url = url.substring(url.indexOf("?") + 1);
        for (String s : url.split("&")) {
            if (s.contains("=")) {
                result.put(s.split("=")[0], s.split("=")[1]);
            }
        }
        return result;
    }

    private String toUrl(Map<String, String> resultMap) {
        StringBuilder url = new StringBuilder();
        for (String key : resultMap.keySet()) {
            if ("game_biz".equals(key)) {
                resultMap.put(key, "hk4e_cn");
            }
            url.append(key).append("=").append(resultMap.get(key)).append("&");
        }
        return url.substring(0, url.length() - 2);
    }

    private void doWish(String query, int what) {
        List<String> type = Arrays.asList("", "301", "200", "302");
        if (what > 0 && what <= 3) {
            requestHistory(query + type.get(what),  (wishList) -> {
                wishList = handleWishCache(wishList, type.get(what));
                String data = GsonUtil.toJson(Optional.ofNullable(wishList).orElse(new ArrayList<>()));
                sendMessage(what, data, handler1);
                doWish(query, what + 1);
            });
        }
    }

    private List<WishVo> handleWishCache(List<WishVo> wishList, String type) {
        if (wishList == null || wishList.isEmpty()) {
            return wishList;
        }
        String id = wishList.get(0).getUid();
        // 处理uid
        SharedPreferences paimon = getSharedPreferences("paimon", MODE_PRIVATE);
        String uid = paimon.getString("uid", "[]");
        List<String> uids = GsonUtil.jsonToList(uid, String.class);
        uids.add(id);
        paimon.edit().putString("uid", GsonUtil.toJson(uids)).apply();
        // 处理祈愿历史记录
        String history = paimon.getString(id + "-" + type, "");
        if (!history.isEmpty()) {
            List<WishVo> cachedWish = GsonUtil.jsonToList(history, WishVo.class);
            Set<String> ids = cachedWish.stream().map(WishVo::getId).collect(Collectors.toSet());
            wishList = wishList.stream().filter(wish -> !ids.contains(wish.getId())).collect(Collectors.toList());
            cachedWish.addAll(0, wishList);
            paimon.edit().putString(id + "-" + type, GsonUtil.toJson(cachedWish)).apply();
            return cachedWish;
        }
        paimon.edit().putString(id + "-" + type, GsonUtil.toJson(wishList)).apply();
        return wishList;
    }

    private void showCharacter(List<WishVo> wishVo) {
        Map<String, Object> result = analysis(wishVo);
        TextView partOverview = findViewById(R.id.part_overview);
        TextView partFiveNum = findViewById(R.id.part_five_num);
        TextView partFourNum = findViewById(R.id.part_four_num);
        TextView partThreeNum = findViewById(R.id.part_three_num);
        TextView partFivePro = findViewById(R.id.part_five_pro);
        TextView partFourPro = findViewById(R.id.part_four_pro);
        TextView partThreePro = findViewById(R.id.part_three_pro);
        TextView partFiveAvg = findViewById(R.id.part_five_avg);
        TextView partFourAvg = findViewById(R.id.part_four_avg);
        partFiveNum.setText((String) result.get("five_num"));
        partFourNum.setText((String) result.get("four_num"));
        partThreeNum.setText((String) result.get("three_num"));
        partFivePro.setText((String) result.get("five_pro"));
        partFourPro.setText((String) result.get("four_pro"));
        partThreePro.setText((String) result.get("three_pro"));
        String five_avg = (String) result.get("five_avg");
        partFiveAvg.setText(five_avg);
        String four_avg = (String) result.get("four_avg");
        partFourAvg.setText(four_avg);
        // 概览样式
        SpannableStringBuilder overviewStyle = getOverviewStyle(wishVo, (String) result.get("overview"));
        partOverview.setText(overviewStyle);
        // 五星平均出货抽数样式
        SpannableStringBuilder fiveExpendStyle = new SpannableStringBuilder(five_avg);
        fiveExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, five_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        partFiveAvg.setText(fiveExpendStyle);
        // 四星平均出货抽数样式
        SpannableStringBuilder fourExpendStyle = new SpannableStringBuilder(four_avg);
        fourExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, four_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        partFourAvg.setText(fourExpendStyle);
        // 设置五星出货顺序
        addSequence((List<WishVo>) result.get("five_seq"), findViewById(R.id.part_five_seq), "金");
        // 设置四星出货顺序
        addSequence((List<WishVo>) result.get("four_seq"), findViewById(R.id.character_four_seq), "紫");
    }

    private void showStandard(List<WishVo> wishVo) {
        TextView standardOverview = findViewById(R.id.standard_overview);
        TextView standardFiveNum = findViewById(R.id.standard_five_num);
        TextView standardFourNum = findViewById(R.id.standard_four_num);
        TextView standardThreeNum = findViewById(R.id.standard_three_num);
        TextView standardFivePro = findViewById(R.id.standard_five_pro);
        TextView standardFourPro = findViewById(R.id.standard_four_pro);
        TextView standardThreePro = findViewById(R.id.standard_three_pro);
        TextView standardFiveAvg = findViewById(R.id.standard_five_avg);
        TextView standardFourAvg = findViewById(R.id.standard_four_avg);
        Map<String, Object> result = analysis(wishVo);
        standardFiveNum.setText((String) result.get("five_num"));
        standardFourNum.setText((String) result.get("four_num"));
        standardThreeNum.setText((String) result.get("three_num"));
        standardFivePro.setText((String) result.get("five_pro"));
        standardFourPro.setText((String) result.get("four_pro"));
        standardThreePro.setText((String) result.get("three_pro"));
        String five_avg = (String) result.get("five_avg");
        standardFiveAvg.setText(five_avg);
        String four_avg = (String) result.get("four_avg");
        standardFourAvg.setText(four_avg);
        // 概览样式
        SpannableStringBuilder overviewStyle = getOverviewStyle(wishVo, (String) result.get("overview"));
        standardOverview.setText(overviewStyle);
        // 五星平均出货抽数样式
        SpannableStringBuilder fiveExpendStyle = new SpannableStringBuilder(five_avg);
        fiveExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, five_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        standardFiveAvg.setText(fiveExpendStyle);
        // 四星平均出货抽数样式
        SpannableStringBuilder fourExpendStyle = new SpannableStringBuilder(four_avg);
        fourExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, four_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        standardFourAvg.setText(fourExpendStyle);
        // 设置五星出货顺序
        addSequence((List<WishVo>) result.get("five_seq"), findViewById(R.id.standard_five_seq), "金");
        // 设置四星出货顺序
        addSequence((List<WishVo>) result.get("four_seq"), findViewById(R.id.standard_four_seq), "紫");
    }

    private SpannableStringBuilder getOverviewStyle(List<WishVo> wishVo, String overview) {
        SpannableStringBuilder overviewStyle = new SpannableStringBuilder(overview);
        int length = (wishVo.size() + "").length();
        int first = overview.indexOf("抽");
        int second = overview.indexOf("抽", first + 1);
        int third = overview.indexOf("抽", second + 1);
        overviewStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 3, 3 + length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        overviewStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9 + length, second, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        overviewStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), second + 7, third, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return overviewStyle;
    }

    private void showWeapon(List<WishVo> wishVo) {
        TextView weaponOverview = findViewById(R.id.weapon_overview);
        TextView weaponFiveNum = findViewById(R.id.weapon_five_num);
        TextView weaponFourNum = findViewById(R.id.weapon_four_num);
        TextView weaponThreeNum = findViewById(R.id.weapon_three_num);
        TextView weaponFivePro = findViewById(R.id.weapon_five_pro);
        TextView weaponFourPro = findViewById(R.id.weapon_four_pro);
        TextView weaponThreePro = findViewById(R.id.weapon_three_pro);
        TextView weaponFiveAvg = findViewById(R.id.weapon_five_avg);
        TextView weaponFourAvg = findViewById(R.id.weapon_four_avg);
        Map<String, Object> result = analysis(wishVo);
        weaponFiveNum.setText((String) result.get("five_num"));
        weaponFourNum.setText((String) result.get("four_num"));
        weaponThreeNum.setText((String) result.get("three_num"));
        weaponFivePro.setText((String) result.get("five_pro"));
        weaponFourPro.setText((String) result.get("four_pro"));
        weaponThreePro.setText((String) result.get("three_pro"));
        String five_avg = (String) result.get("five_avg");
        weaponFiveAvg.setText(five_avg);
        String four_avg = (String) result.get("four_avg");
        weaponFourAvg.setText(four_avg);
        // 概览样式
        SpannableStringBuilder overviewStyle = getOverviewStyle(wishVo, (String) result.get("overview"));
        weaponOverview.setText(overviewStyle);
        // 五星平均出货抽数样式
        SpannableStringBuilder fiveExpendStyle = new SpannableStringBuilder(five_avg);
        fiveExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, five_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        weaponFiveAvg.setText(fiveExpendStyle);
        // 四星平均出货抽数样式
        SpannableStringBuilder fourExpendStyle = new SpannableStringBuilder(four_avg);
        fourExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, four_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        weaponFourAvg.setText(fourExpendStyle);
        // 设置五星出货顺序
        addSequence((List<WishVo>) result.get("five_seq"), findViewById(R.id.weapon_five_seq), "金");
        // 设置四星出货顺序
        addSequence((List<WishVo>) result.get("four_seq"), findViewById(R.id.weapon_four_seq), "紫");
    }

    private void addSequence(List<WishVo> wishList, LinearLayout content, String color) {
        // 一行能放字数极限
        int limit = 36;
        LinearLayout line = null;
        if (wishList.isEmpty()) {
            content.addView(generateTextView(R.color.purple_200, "还未出" + color));
        }
        content.removeAllViews();
        for (WishVo wishVo : wishList) {
            // 四星模拟数据跳过
            if ("紫".equals(color) && "-1".equals(wishVo.getId())) {
                continue;
            }
            // 一行剩余升放的字数
            int length = wishVo.getName().length() * 2 + wishVo.getCount().length() + 2;
            limit -= length;
            if (line == null || limit < 0) {
                line = new LinearLayout(MainActivity.this);
                LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lLayoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);
                line.setLayoutParams(lLayoutParams);
                content.addView(line);
            }
            // 计算颜色
            Integer colorId = CharacterStyle.get(wishVo.getName());
            if (colorId == null) {
                if ("4".equals(wishVo.getRank_type())) {
                    colorId = R.color.purple_200;
                }
                if ("5".equals(wishVo.getRank_type())) {
                    colorId = R.color.gold;
                }
                colorId = colorId == null ? R.color.default_color : colorId;
            }
            // 将EditText放到LinearLayout里
            line.addView(generateTextView(colorId, wishVo.getName() + "(" + wishVo.getCount() + ")"));
            limit = limit < 0 ? 36 - length : limit;
        }
    }

    private TextView generateTextView(int color, String text) {
        // 单个角色
        TextView textView = new TextView(MainActivity.this);
        LinearLayout.LayoutParams etParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        etParam.setMargins(0, SystemUtil.Dp2Px(this, 10), SystemUtil.Dp2Px(this, 6), 0);
        textView.setLayoutParams(etParam);
        // 设置属性
        Resources resources = getBaseContext().getResources();
        textView.setTextColor(resources.getColor(color));
        textView.setText(text);
        return textView;
    }

    private Map<String, Object> analysis(List<WishVo> wishList) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", wishList.size() + "");
        // 五星角色
        List<WishVo> fivePart = getCharacterSequence(wishList, 5);
        // 四星角色
        List<WishVo> fourPart = getCharacterSequence(wishList, 4);
        // 五星出货顺序
        result.put("five_seq", fivePart);
        // 四星出货顺序
        result.put("four_seq", fourPart);
        // 五星数量
        long five = fivePart.size();
        result.put("five_num", "五星：" + (five == 0L ? "还未出金" : five));
        result.put("five_pro", "【 " + (wishList.size() == 0 ? "0": round(five * 1.0 / wishList.size())) + "% 】");
        // 四星数量
        long four = wishList.stream().filter(wishVo -> "4".equals(wishVo.getRank_type())).count();
        result.put("four_num", "四星：" + (four == 0L ? "还未出紫" : four));
        result.put("four_pro", "【 " + (wishList.size() == 0 ? "0": round(four * 1.0 / wishList.size())) + "% 】");
        // 三星数量
        long three = wishList.stream().filter(wishVo -> "3".equals(wishVo.getRank_type())).count();
        result.put("three_num", "三星：" + (three == 0L ? "还未出蓝" : three));
        result.put("three_pro", "【 " + (wishList.size() == 0 ? "0": round(three * 1.0 / wishList.size())) + "% 】");
        // 五星平均出货抽数
        Double five_avg = fivePart.stream().map(WishVo::getCount).map(Integer::parseInt).collect(Collectors.averagingInt((Integer::intValue)));
        result.put("five_avg", "五星平均出货抽数：" + (five == 0L ? "还未出金" : round(five_avg / 100)));
        // 四星平均出货抽数
        Double four_avg = fourPart.stream().map(WishVo::getCount).map(Integer::parseInt).collect(Collectors.averagingInt((Integer::intValue)));
        result.put("four_avg", "四星平均出货抽数：" + (four == 0L ? "还未出紫" : round( four_avg / 100)));
        // 概览（多少抽未出紫，多少抽未出金）
        result.put("overview", getOverview(wishList));
        return result;
    }

    /**
     * 获取角色出货顺序，及抽取所花费抽数
     */
    private List<WishVo> getCharacterSequence(List<WishVo> wishList, int level) {
        Collections.reverse(wishList);
        int expend = 0;
        List<WishVo> sequence = new ArrayList<>();
        for (WishVo wishVo : wishList) {
            expend++;
            if ((level + "").equals(wishVo.getRank_type())) {
                wishVo.setCount(expend + "");
                sequence.add(wishVo);
                expend = 0;
            }
        }
        Collections.reverse(wishList);
        Collections.reverse(sequence);
        return sequence;
    }

    private String getOverview(List<WishVo> wishList) {
        int noGold = 0, noPurple = 0;
        boolean gold = false, purple = false;
        for (WishVo wishVo : wishList) {
            if (!"4".equals(wishVo.getRank_type()) && !purple) {
                noPurple ++;
            }
            if ("4".equals(wishVo.getRank_type())) {
                purple = true;
            }
            if (!"5".equals(wishVo.getRank_type()) && !gold) {
                noGold ++;
            }
            if ("5".equals(wishVo.getRank_type())) {
                gold = true;
            }
            if (gold && purple) {
                break;
            }
        }
        return "一共 " + wishList.size() + " 抽，已累计 " + noGold + " 抽未出金，累计 " + noPurple + " 抽未出紫";
    }

    private String round(double num) {
        return String.format(Locale.CHINA, "%.1f", num * 100);
    }

    // 请求祈愿记录
    private void requestHistory(String urlQuery, RequestHandler handler) {
        doRequest(0, "0", urlQuery, new ArrayList<>(), 100, handler);
    }

    private void doRequest(int page, String endId, String urlQuery, List<WishVo> wishList, int interval, RequestHandler handler) {
        String url = "https://hk4e-api.mihoyo.com/event/gacha_info/api/getGachaLog?" + urlQuery + "&page=" + page + "&size=20&end_id=" + endId;
        new HttpUtil().get(url, RequestResult.class, new HttpCallBack<RequestResult>() {
            @Override
            public void onSuccess(RequestResult result) {
                try {
                    if (result.isOk()) {
                        List<WishVo> list = result.getData().getList();
                        wishList.addAll(list);
                        if (list.size() == 20) {
                            WishVo last = list.get(list.size() - 1);
                            Thread.sleep((int) (Math.random() * 200) + interval);
                            doRequest(page + 1, last.getId(), urlQuery, wishList, interval, handler);
                        } else {
                            handler.onComplete(wishList);
                        }
                    } else {
                        if ("authkey timeout".equals(result.getMessage())) {
                            showDialog("链接已失效，请重新从游戏中复制");
                        }
                        // 如果系统报请求频繁，则将每次请求时间间隔+100ms重新请求
                        if ("visit too frequently".equals(result.getMessage())) {
                            doRequest(0, "0", urlQuery, new ArrayList<>(), interval + 100, handler);
                        }
                        Log.d(GsonUtil.toJson(result));
                    }
                } catch (InterruptedException e) {
                    Log.e(e);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e(StringUtil.TAG, message);
            }
        });
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("提示").setMessage(message)
                .setIcon(R.mipmap.favicon)
                .setPositiveButton("确定", (dialog, which) -> {})
                .create().show();
    }
}