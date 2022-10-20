package com.example.paimon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.example.paimon.util.AuthKeyUtil;
import com.example.paimon.util.CharacterStyle;
import com.example.paimon.util.GsonUtil;
import com.example.paimon.util.HttpCallBack;
import com.example.paimon.util.HttpUtil;
import com.example.paimon.util.Log;
import com.example.paimon.util.StringUtil;
import com.example.paimon.util.SystemUtil;

import java.util.*;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private String currentAccount = null;

    // 是否海外版
    private boolean sea = false;

    private final Handler handler1 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String data = msg.getData().getString("data");
            List<WishVo> wishList = GsonUtil.jsonToList(data, WishVo.class);
            switch (msg.what) {
                case 1: showDetail(wishList, "character", "301");
                    Toast.makeText(MainActivity.this, "角色池加载完成", Toast.LENGTH_SHORT).show();
                    break;
                case 2: showDetail(wishList, "standard", "200");
                    Toast.makeText(MainActivity.this, "常驻池加载完成", Toast.LENGTH_SHORT).show();
                    break;
                case 3: showDetail(wishList, "weapon", "302");
                    Toast.makeText(MainActivity.this, "武器池加载完成", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final Handler handler2 = new Handler() {
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
        SharedPreferences cache = CommUtil.getInstance().getSharedPreferences(this);
        String cacheUrl = cache.getString("content", "");
        content.setText(cacheUrl);
        // 监听按钮点击事件
        setOnClickListener(content, cache);
        List<String> uids = GsonUtil.jsonToList(cache.getString("uid", "[]"), String.class);
        createAccount(uids);
        if (!uids.isEmpty()) {
            showCacheRecord(uids.get(0));
        }
        // 申请存储权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1024);
            }
        }
        // 页面跳转
        findViewById(R.id.tips).setOnClickListener((view) -> {
            Intent tipsIntent = new Intent(this, CookieActivity.class);
            startActivity(tipsIntent);
        });
        findViewById(R.id.activity_main_title_tip).setOnClickListener((view) -> {
            Intent tipsIntent = new Intent(this, CookieActivity.class);
            startActivity(tipsIntent);
        });

        // 获取最新角色配置信息
        CharacterStyle.pullConfig(this);
        // 检查更新
        CommUtil.getInstance().checkUpdate(this, true);
    }

    private boolean accountToTop(String account) {
        SharedPreferences cache = CommUtil.getInstance().getSharedPreferences(this);
        List<String> uids = GsonUtil.jsonToList(cache.getString("uid", "[]"), String.class);
        if (uids.contains(account)) {
            uids.remove(account);
            uids.add(0, account);
            cache.edit().putString("uid", GsonUtil.toJson(uids)).apply();
            createAccount(uids);
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void changeElementHideOrShow(String prefix, String code, String id) {
        LinearLayout element = findViewById(id);
        element.removeAllViews();
        String showStr = getResources().getString(R.string.show_four_sequence);
        TextView textView = CommUtil.getInstance().generateTextView(this, R.color.blue, showStr);
        element.addView(textView);
        textView.setOnClickListener((view) -> {
            String character = CommUtil.getInstance().readCacheFile(this, currentAccount + "-" + code + ".json", "[]");
            List<WishVo> wishVos = GsonUtil.jsonToList(character, WishVo.class);
            Map<String, Object> result = analysis(wishVos);
            addSequence((List<WishVo>) result.get("four_seq"), findViewById(prefix + "_four_seq"), "紫");
        });
    }

    private void setOnClickListener(TextView contentView, SharedPreferences cache) {
        TextView button = findViewById(R.id.button);
        button.setOnClickListener((view) -> {
            String content = contentView.getText().toString();
            // 从粘贴的字符串中找出URL链接
            int beginIndex = content.indexOf("https://webstatic.mihoyo.com");
            sea = beginIndex == -1;
            if (beginIndex == -1) {
                beginIndex = content.indexOf("https://webstatic-sea.hoyoverse.com/");
            }
            int endIndex = content.indexOf("#/log");
            if (beginIndex == -1 || endIndex == -1 || beginIndex >= endIndex) {
                Toast.makeText(this, "输入的链接有误，请检查", Toast.LENGTH_SHORT).show();
                return;
            }
            // 裁剪
            String url = content.substring(beginIndex, endIndex + 5);
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
                    CommUtil.getInstance().showDialog(MainActivity.this, "输入的链接有误，请检查");
                }
            } else {
                CommUtil.getInstance(). showDialog(MainActivity.this, "请输入抽卡记录链接！");
            }
        });
        // 切换四星抽卡顺序的显示与隐藏
        findViewById(R.id.character_four_seq_text).setOnClickListener((view) -> {
            changeElementHideOrShow("character", "301", "character_four_seq");
        });
        findViewById(R.id.standard_four_seq_text).setOnClickListener((view) -> {
            changeElementHideOrShow("standard", "200", "standard_four_seq");
        });
        findViewById(R.id.weapon_four_seq_text).setOnClickListener((view) -> {
            changeElementHideOrShow("weapon", "302", "weapon_four_seq");
        });
        findViewById(R.id.account_question).setOnClickListener((view) -> {
            CommUtil.getInstance().showDialog(this, "长按账号可将其置顶，下次进来app将首先展示该账号信息");
        });
        findViewById(R.id.update).setOnClickListener((view) -> {
            // 响应之前不允许再次点击
            view.setClickable(false);
            CommUtil.getInstance().checkUpdate(MainActivity.this, false);
        });
    }

    private void createAccount(List<String> uids) {
        LinearLayout account = findViewById(R.id.account);
        account.removeAllViews();
        if (!uids.isEmpty()) {
            for (List<String> list : CommUtil.getInstance().splitList(uids, 4)) {
                // 一行
                LinearLayout line = new LinearLayout(MainActivity.this);
                LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lLayoutParams.setLayoutDirection(LinearLayout.HORIZONTAL);
                line.setLayoutParams(lLayoutParams);
                for (String No :list) {
                    TextView accountText = CommUtil.getInstance().generateTextView(this, R.color.blue, No);
                    accountText.setId(R.id.spline);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) accountText.getLayoutParams();
                    layoutParams.setMargins(0, SystemUtil.Dp2Px(this, -2), SystemUtil.Dp2Px(this, 15), SystemUtil.Dp2Px(this, 15));
                    accountText.setTextSize(SystemUtil.Dp2Px(this, 4.5f));
                    accountText.setOnClickListener((view) -> showCacheRecord(((TextView) view).getText().toString()));
                    accountText.setOnLongClickListener((view) -> accountToTop(((TextView) view).getText().toString()));
                    line.addView(accountText);
                }
                account.addView(line);
            }
        } else {
             account.addView(CommUtil.getInstance().generateNormalTextView(this, null, "暂无账号可切换"));
        }
    }

    private  <T extends View> T findViewById(String name) {
        int id = getResources().getIdentifier(name, "id", getPackageName());
        return findViewById(id);
    }

    private void showCacheRecord(String uid) {
        currentAccount = uid;
        // 角色池
        String character = CommUtil.getInstance().readCacheFile(this, uid + "-301.json", "[]");
        showDetail(GsonUtil.jsonToList(character, WishVo.class), "character", "301");
        // 常驻池
        String standard = CommUtil.getInstance().readCacheFile(this, uid + "-200.json", "[]");
        showDetail(GsonUtil.jsonToList(standard, WishVo.class), "standard", "200");
        // 武器池
        String weapon = CommUtil.getInstance().readCacheFile(this, uid + "-302.json", "[]");
        showDetail(GsonUtil.jsonToList(weapon, WishVo.class), "weapon", "302");
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
        SharedPreferences cache = CommUtil.getInstance().getSharedPreferences(this);
        List<String> uids = GsonUtil.jsonToList(cache.getString("uid", "[]"), String.class);
        if (!uids.contains(uid)) {
            uids.add(uid);
        }
        cache.edit().putString("uid", GsonUtil.toJson(uids)).apply();
        createAccount(uids);
        // 处理祈愿历史记录
        String history = CommUtil.getInstance().readCacheFile(this, uid + "-" + type + ".json", "[]");
        if (!history.isEmpty()) {
            List<WishVo> cachedWish = GsonUtil.jsonToList(history, WishVo.class);
            Set<String> ids = cachedWish.stream().map(WishVo::getId).collect(Collectors.toSet());
            wishList = wishList.stream().filter(wish -> !ids.contains(wish.getId())).collect(Collectors.toList());
            cachedWish.addAll(0, wishList);
            CommUtil.getInstance().writeCacheFile(this, GsonUtil.toJson(cachedWish), uid + "-" + type + ".json");
            return cachedWish;
        }
        CommUtil.getInstance().writeCacheFile(this, GsonUtil.toJson(wishList), uid + "-" + type + ".json");
        return wishList;
    }

    public void showDetail(List<WishVo> wishVo, String prefix, String code) {
        clearDetail(prefix);
        if (wishVo.isEmpty()) {
            return;
        }
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
        // 角色活动祈愿才有up五星的说法
        if ("character".equals(prefix)) {
            TextView partFiveAvgUp = findViewById(R.id.character_five_avg_up);
            String five_avg_up = (String) result.get("five_avg_up");
            partFiveAvgUp.setText(five_avg_up);
            // up五星平均出货抽数样式
            SpannableStringBuilder upFiveExpendStyle = new SpannableStringBuilder(five_avg_up);
            upFiveExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 11, five_avg_up.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            partFiveAvgUp.setText(upFiveExpendStyle);
        }
        // 四星平均出货抽数样式
        SpannableStringBuilder fourExpendStyle = new SpannableStringBuilder(four_avg);
        fourExpendStyle.setSpan(new ForegroundColorSpan(Color.MAGENTA), 9, four_avg.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        partFourAvg.setText(fourExpendStyle);
        // 设置五星出货顺序
        addSequence((List<WishVo>) result.get("five_seq"), findViewById(prefix + "_five_seq"), "金");
        // 设置四星出货顺序
//        addSequence((List<WishVo>) result.get("four_seq"), findViewById(prefix + "_four_seq"), "紫");
        LinearLayout element = findViewById(prefix + "_four_seq");
        String showStr = getResources().getString(R.string.show_four_sequence);
        TextView textView = CommUtil.getInstance().generateTextView(this, R.color.blue, showStr);
        element.addView(textView);
        textView.setOnClickListener((view) -> {
//            String character = CommUtil.getInstance().readCacheFile(this, currentAccount + "-" + code + ".json", "[]");
//            showDetail(GsonUtil.jsonToList(character, WishVo.class), prefix, code);
            addSequence((List<WishVo>) result.get("four_seq"), findViewById(prefix + "_four_seq"), "紫");
        });
    }

    private void clearDetail(String prefix) {
        TextView overview = findViewById(prefix + "_overview");
        TextView fiveNum = findViewById(prefix + "_five_num");
        TextView fourNum = findViewById(prefix + "_four_num");
        TextView threeNum = findViewById(prefix + "_three_num");
        TextView fivePro = findViewById(prefix + "_five_pro");
        TextView fourPro = findViewById(prefix + "_four_pro");
        TextView threePro = findViewById(prefix + "_three_pro");
        TextView fiveAvg = findViewById(prefix + "_five_avg");
        TextView fourAvg = findViewById(prefix + "_four_avg");
        LinearLayout fiveSeq = findViewById(prefix + "_five_seq");
        LinearLayout fourSeq = findViewById(prefix + "_four_seq");
        overview.setText(getResources().getString(R.string.overview));
        fiveNum.setText(getResources().getString(R.string.five_level));
        fourNum.setText(getResources().getString(R.string.four_level));
        threeNum.setText(getResources().getString(R.string.three_level));
        fivePro.setText(getResources().getString(R.string.empty_probability));
        fourPro.setText(getResources().getString(R.string.empty_probability));
        threePro.setText(getResources().getString(R.string.empty_probability));
        fiveAvg.setText(getResources().getString(R.string.five_avg));
        fourAvg.setText(getResources().getString(R.string.four_avg));
        fiveSeq.removeAllViews();
        fourSeq.removeAllViews();
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
        int limit = 32;
        LinearLayout line = null;
        if (wishList.isEmpty()) {
            content.addView(CommUtil.getInstance().generateTextView(this, R.color.purple_200, "还未出" + color));
        }
        content.removeAllViews();
        int remain = limit;
        for (WishVo wishVo : wishList) {
            // 四星模拟数据跳过
            if ("紫".equals(color) && "-1".equals(wishVo.getId())) {
                continue;
            }
            // 一行剩余能放的字数（+2：两个括号，+1：空格）
            int length = wishVo.getName().length() * 2 + wishVo.getCount().length() + 2 + 1;
            remain -= length;
            if (line == null || remain < 0) {
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
            line.addView(CommUtil.getInstance().generateTextView(this, colorId, wishVo.getName() + "(" + wishVo.getCount() + ")"));
            remain = remain < 0 ? limit - length : remain;
        }
    }

    private Map<String, Object> analysis(List<WishVo> wishList) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", wishList.size() + "");
        Set<String> standardCharacter = new HashSet<>(Arrays.asList("迪卢克", "琴", "莫娜", "刻晴", "七七"));
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
        IntSummaryStatistics five_total = fivePart.stream().map(WishVo::getCount).map(Integer::parseInt).collect(Collectors.summarizingInt(Integer::intValue));
        result.put("five_avg", "五星平均出货抽数：" + (five == 0L ? "还未出金" : round(five_total.getAverage() / 100)));
        // up五星平均出货抽数
        long up_five_num = fivePart.stream().filter(wishVo -> !standardCharacter.contains(wishVo.getName())).count();
        result.put("five_avg_up", "up五星平均出货抽数：" + (five == 0L || up_five_num == 0L ? "还未出up五星" : round(five_total.getSum() / up_five_num * 1.0 / 100)));
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

    @SuppressLint("StringFormatMatches")
    private void doRequest(int page, String endId, String urlQuery, List<WishVo> wishList, int interval, RequestHandler handler) {
        String WISH_URL_TEMPLATE = getResources().getString(R.string.url_wish_template);
        if (sea) {
            WISH_URL_TEMPLATE = getResources().getString(R.string.url_wish_sea_template);
        }
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
                            CommUtil.getInstance().showDialog(MainActivity.this, "链接已失效，请重新从游戏中复制");
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

}
