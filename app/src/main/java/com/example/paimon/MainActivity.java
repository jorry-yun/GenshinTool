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
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final String CACHE_NAME = "paimon";

    private final String WISH_URL_TEMPLATE = "https://hk4e-api.mihoyo.com/event/gacha_info/api/getGachaLog?%s&page=%s&size=20&end_id=%s";

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String data = msg.getData().getString("data");
            String uid = msg.getData().getString("uid");
            List<WishVo> wishList = GsonUtil.jsonToList(data, WishVo.class);
            switch (msg.what) {
                case 1: showDetail(wishList, "character");
                    Toast.makeText(MainActivity.this, "角色池加载完成", Toast.LENGTH_SHORT).show();
                    CommUtil.getInstance().writeCacheFile(data, getResources().getString(R.string.cache_path) + uid + "-301.json");
                    break;
                case 2: showDetail(wishList, "standard");
                    Toast.makeText(MainActivity.this, "常驻池加载完成", Toast.LENGTH_SHORT).show();
                    CommUtil.getInstance().writeCacheFile(data, getResources().getString(R.string.cache_path) + uid + "-200.json");
                    break;
                case 3: showDetail(wishList, "weapon");
                    Toast.makeText(MainActivity.this, "武器池加载完成", Toast.LENGTH_SHORT).show();
                    CommUtil.getInstance().writeCacheFile(data, getResources().getString(R.string.cache_path) + uid + "-302.json");
                    break;
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
        SharedPreferences cache = getSharedPreferences(CACHE_NAME, MODE_PRIVATE);
        String cacheUrl = cache.getString("content", "");
        content.setText(cacheUrl);
        // 监听按钮点击事件
        setOnClickListener(content, cache);
        List<String> uids = GsonUtil.jsonToList(cache.getString("uid", "[]"), String.class);
        createAccount(cache, uids);
        if (!uids.isEmpty()) {
            showCacheRecord(uids.get(0), cache);
        }

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
//                    vo.setName("");
//                    vo.setRank_type("5");
//                }
//                data.add(vo);
//            }
//            cache.edit().putString("100049717-301", GsonUtil.toJson(data)).apply();
//            Toast.makeText(this, "模拟数据生成成功", Toast.LENGTH_SHORT).show();
//        });

        // 页面跳转
        TextView tips = findViewById(R.id.tips);
        tips.setOnClickListener((view) -> {
            Intent tipsIntent = new Intent(this, TipActivity.class);
            startActivity(tipsIntent);

        });
    }

    private void setOnClickListener(EditText content, SharedPreferences cache) {
        TextView button = findViewById(R.id.button);
        button.setOnClickListener((view) -> {
            String url = content.getText().toString();
            if (url.length() != 0) {
                Map<String, String> resultMap = CommUtil.getInstance().parseUrl(url);
                String authKey = resultMap.get("authkey");
                if (authKey != null && authKey.length() > 0) {
                    // 设置缓存
                    cache.edit().putString("content", url).apply();
                    Toast.makeText(this, "正在获取祈愿池信息，时间可能有点长，请耐心等待", Toast.LENGTH_SHORT).show();
                    String urlQuery = CommUtil.getInstance().toUrl(resultMap);
                    CommUtil.getInstance().sendMessage(0, urlQuery + "&gacha_type=", handler2);
                } else {
                    showDialog("输入的链接有误，请检查");
                }
            } else {
                showDialog("请输入抽卡记录链接！");
            }
        });
    }

    private void createAccount(SharedPreferences cache, List<String> uids) {
        LinearLayout account = findViewById(R.id.account);
        account.removeAllViews();
        if (!uids.isEmpty()) {
            for (List<String> list : CommUtil.getInstance().splitList(uids, 3)) {
                // 一行
                LinearLayout line = new LinearLayout(MainActivity.this);
                LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lLayoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);
                line.setLayoutParams(lLayoutParams);
                for (String No :list) {
                    TextView accountText = generateTextView(R.color.blue, No);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) accountText.getLayoutParams();
                    layoutParams.setMargins(0, SystemUtil.Dp2Px(this, -2), SystemUtil.Dp2Px(this, 15), SystemUtil.Dp2Px(this, 15));
                    accountText.setTextSize(SystemUtil.Dp2Px(this, 4.5f));
                    accountText.setOnClickListener((view) -> showCacheRecord(((TextView) view).getText().toString(), cache));
                    line.addView(accountText);
                }
                account.addView(line);
            }
        } else {
             account.addView(generateTextView(R.color.black, "暂无账号可切换"));
        }
    }

    private  <T extends View> T findViewById(String name) {
        int id = getResources().getIdentifier(name, "id", getPackageName());
        return findViewById(id);
    }

    private void showCacheRecord(String uid, SharedPreferences cache) {
        String prefix_path = getResources().getString(R.string.cache_path) + uid;
        // 角色池
        String character = cache.getString(uid + "-301", "[]");
        String character_cache = CommUtil.getInstance().readCacheFile(prefix_path + "-301.json");
        showDetail(mixWishData(character, character_cache), "character");
        // 常驻池
        String standard = cache.getString(uid + "-200", "[]");
        String standard_cache = CommUtil.getInstance().readCacheFile(prefix_path + "-200.json");
        showDetail(mixWishData(standard, standard_cache), "standard");
        // 武器池
        String weapon = cache.getString(uid + "-302", "[]");
        String weapon_cache = CommUtil.getInstance().readCacheFile(prefix_path + "-302.json");
        showDetail(mixWishData(weapon, weapon_cache), "weapon");
    }

    private List<WishVo> mixWishData(String wish, String cache) {
        List<WishVo> mixed = GsonUtil.jsonToList(wish, WishVo.class);
        List<WishVo> cacheWish = GsonUtil.jsonToList(cache, WishVo.class);
        mixed.addAll(cacheWish);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TreeSet<WishVo> result = new TreeSet<>((o1, o2) -> {
            try {
                return dateFormat.parse(o1.getTime()).compareTo(dateFormat.parse(o2.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        result.addAll(mixed);
        return new ArrayList<>(result);
    }

    private void doWish(String query, int what) {
        List<String> type = Arrays.asList("", "301", "200", "302");
        if (what > 0 && what <= 3) {
            requestHistory(query + type.get(what),  (wishList) -> {
                wishList = handleWishCache(wishList, type.get(what));
                String data = GsonUtil.toJson(Optional.ofNullable(wishList).orElse(new ArrayList<>()));
                CommUtil.getInstance().sendMessage(what, data, handler1);
                doWish(query, what + 1);
            });
        }
    }

    private List<WishVo> handleWishCache(List<WishVo> wishList, String type) {
        if (wishList == null || wishList.isEmpty()) {
            return wishList;
        }
        String uid = wishList.get(0).getUid();
        // 处理uid
        SharedPreferences cache = getSharedPreferences(CACHE_NAME, MODE_PRIVATE);
        List<String> uids = GsonUtil.jsonToList(cache.getString("uid", "[]"), String.class);
        if (!uids.contains(uid)) {
            uids.add(uid);
        }
        cache.edit().putString("uid", GsonUtil.toJson(uids)).apply();
        createAccount(cache, uids);
        // 处理祈愿历史记录
        String history = cache.getString(uid + "-" + type, "");
        if (!history.isEmpty()) {
            List<WishVo> cachedWish = GsonUtil.jsonToList(history, WishVo.class);
            Set<String> ids = cachedWish.stream().map(WishVo::getId).collect(Collectors.toSet());
            wishList = wishList.stream().filter(wish -> !ids.contains(wish.getId())).collect(Collectors.toList());
            cachedWish.addAll(0, wishList);
            cache.edit().putString(uid + "-" + type, GsonUtil.toJson(cachedWish)).apply();
            return cachedWish;
        }
        cache.edit().putString(uid + "-" + type, GsonUtil.toJson(wishList)).apply();
        return wishList;
    }

    public void showDetail(List<WishVo> wishVo, String prefix) {
        Map<String, Object> result = analysis(wishVo);
        TextView partOverview = findViewById(prefix + "_overview");
        TextView partFiveNum = findViewById(prefix + "_five_num");
        TextView partFourNum = findViewById(prefix + "_four_num");
        TextView partThreeNum = findViewById(prefix + "_three_num");
        TextView partFivePro = findViewById(prefix + "_five_pro");
        TextView partFourPro = findViewById(prefix + "_four_pro");
        TextView partThreePro = findViewById(prefix + "_three_pro");
        TextView partFiveAvg = findViewById(prefix + "_five_avg");
        TextView partFourAvg = findViewById(prefix + "_four_avg");
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
        addSequence((List<WishVo>) result.get("five_seq"), findViewById(prefix + "_five_seq"), "金");
        // 设置四星出货顺序
        addSequence((List<WishVo>) result.get("four_seq"), findViewById(prefix + "_four_seq"), "紫");
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
        String url = String.format(WISH_URL_TEMPLATE, urlQuery, page, endId);
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
