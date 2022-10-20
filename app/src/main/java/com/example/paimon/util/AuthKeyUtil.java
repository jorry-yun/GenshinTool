package com.example.paimon.util;

import android.os.Handler;
import android.os.Message;

import com.example.paimon.CommUtil;
import com.example.paimon.entity.AuthKeyDataDto;
import com.example.paimon.entity.AuthKeyPostData;
import com.example.paimon.entity.ChouKaObj;
import com.example.paimon.entity.ListUrl;
import com.example.paimon.entity.LoginCookieDataDto;
import com.example.paimon.entity.LoginTokenDto;
import com.example.paimon.entity.UserGameRolesByCookieDataDto;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthKeyUtil {

    private static AuthKeyUtil authKeyUtil;

    public static AuthKeyUtil getInstance() {
        if (authKeyUtil == null) {
            authKeyUtil = new AuthKeyUtil();
        }
        return authKeyUtil;
    }

    OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    public void getAuthkey(String cookie, Handler handler) {
        new Thread(() -> {
            try {
                long time = new Date().getTime();
                Request req = new Request.Builder()
                        .url("https://webapi.account.mihoyo.com/Api/login_by_cookie?t=" + time)
                        .header("Cookie", cookie)
                        .build();
                Call call = client.newCall(req);
                Response response = call.execute();
                String loginCookieDataDtobody = response.body() == null ? null : response.body().string();
                Log.d(String.format("body====>%s", loginCookieDataDtobody));
                Gson gson = new Gson();
                LoginCookieDataDto loginCookieData = gson.fromJson(
                        loginCookieDataDtobody,
                        LoginCookieDataDto.class
                );
                LoginCookieDataDto.Data cookieDataData = loginCookieData.getData();
                if (cookieDataData == null) {
                    sendErrorMsg("请求失败", handler);
                    return;
                }
                if (cookieDataData != null && cookieDataData.getStatus() != 1) {
                    String msg = CommUtil.getInstance().getString(cookieDataData.getMsg(), "登录信息失效，请重新登录");
                    sendErrorMsg(msg, handler);
                    return;
                }
                Integer uid = cookieDataData.getAccount_info().getAccount_id();
                String token = cookieDataData.getAccount_info().getWeblogin_token();

                //获取tid
                Request Multireq = new Request.Builder()
                        .url(String.format("https://api-takumi.mihoyo.com/auth/api/getMultiTokenByLoginTicket?login_ticket=%s&token_types=3&uid=%s", token, uid))
                        .header("Cookie", cookie)
                        .build();
                Call Multicall = client.newCall(Multireq);
                Response multiresponse = Multicall.execute();
                String MultiDataDtobody = multiresponse.body() == null ? null : multiresponse.body().string();
                Log.d(MultiDataDtobody);
                LoginTokenDto multiDataData = gson.fromJson(
                        MultiDataDtobody,
                        LoginTokenDto.class
                );
                String newcookie = String.format("stuid=%s;", uid);
                for (LoginTokenDto.DataObj dataObj : multiDataData.getData().getList()) {
                    newcookie += String.format("%s=%s;", dataObj.getName(), dataObj.getToken());
                }
                newcookie += cookie;
                Log.d(newcookie);
                //获取uid
                Request uidreq = new Request.Builder()
                        .url("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=hk4e_cn")
                        .header("Cookie", newcookie)
                        .build();
                Call uidcall = client.newCall(uidreq);
                Response uidresponse = uidcall.execute();
                String uidDataDtobody = uidresponse.body() == null ? null : uidresponse.body().string();
                Log.d(String.format("uidDataDtobody:=>>>%s", uidDataDtobody));
                UserGameRolesByCookieDataDto UserGameRolesByCookieData = gson.fromJson(
                        uidDataDtobody,
                        UserGameRolesByCookieDataDto.class
                );
                List<ListUrl> listUrl = new ArrayList<>();
                for (UserGameRolesByCookieDataDto.UserService userService: UserGameRolesByCookieData.getData().getList()) {
                    String gameUid = userService.getGame_uid();
                    String nickname = userService.getNickname();
                    String gameBiz = userService.getGame_biz();
                    String region = userService.getRegion();
                    AuthKeyPostData authKeyPostData = new AuthKeyPostData("webview_gacha", gameBiz, gameUid, region);
                    String toJson = gson.toJson(authKeyPostData);
                    RequestBody createRequestBody =
                            RequestBody.create(MediaType.parse("application/json;charset=utf-8"), toJson);
                    Log.d(newcookie);
                    Request authkeyreq = new Request.Builder()
                            .url("https://api-takumi.mihoyo.com/binding/api/genAuthKey")
                            .header("Content-Type", "application/json;charset=utf-8")
                            .header("Host", "api-takumi.mihoyo.com")
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Referer", "https://webstatic.mihoyo.com")
                            .header("x-rpc-app_version", "2.28.1")
                            .header("x-rpc-client_type", "5")
                            .header("x-rpc-device_id", "CBEC8312-AA77-489E-AE8A-8D498DE24E90")
                            .header("x-requested-with", "com.mihoyo.hyperion")
                            .header("DS", getDs())
                            .header("Cookie", newcookie)
                            .post(createRequestBody)
                            .build();
                    Call authkeycall = client.newCall(authkeyreq);
                    Response authkeyresponse = authkeycall.execute();
                    String authkeyDataDtobody = authkeyresponse.body() == null ? null : authkeyresponse.body().string();
                    Log.d(authkeyDataDtobody);
                    AuthKeyDataDto authKeyDataDto = gson.fromJson(
                            authkeyDataDtobody,
                            AuthKeyDataDto.class
                    );
                    AuthKeyDataDto.AuthKeyData keyData = authKeyDataDto.getData();
                    if (keyData == null || !"OK".equals(authKeyDataDto.getMessage())) {
                        sendErrorMsg("获取失败", handler);
                        return;
                    }
                    String authkey = URLEncoder.encode(keyData.getAuthkey(), "utf-8");
                    String url = String.format("https://webstatic.mihoyo.com/hk4e/event/e20190909gacha-v2/index.html?win_mode=fullscreen&authkey_ver=1&sign_type=2&auth_appid=webview_gacha&init_type=200&timestamp=%s&lang=zh-cn&device_type=mobile&plat_type=android&region=%s&authkey=%s&game_biz=%s#/log", time / 1000, region, authkey, gameBiz);
                    Log.d(url);
                    listUrl.add(new ListUrl(String.format("%s(%s)", gameUid, nickname), url));
                }
                ChouKaObj result = new ChouKaObj(200, "请求成功", listUrl);
                //返回主线程
                CommUtil.getInstance().sendMessage(0, GsonUtil.toJson(result), handler);
            } catch (IOException e) {
                sendErrorMsg("获取失败", handler);
            }
        }).start();
    }

    private String getDs() {
        String salt = "ulInCDohgEs557j0VsPDYnQaaz6KJcv5";
        Long time = new Date().getTime() / 1000;
        String str = this.getStr();
        String key = String.format("salt=%s&t=%s&r=%s", salt, time, str);
        String md5 = Md5Util.getMD5(key);
        return String.format("%s,%s,%s", time, str, md5);
    }

    private String getStr() {
        char[] chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678".toCharArray();
        int maxPos = chars.length;
        String code = "";
        for (int i = 0; i <= 5; i++) {
            code += chars[(int) Math.floor(Math.random() * maxPos)];
        }
        return code;
    }

    private void sendErrorMsg(String msg, Handler handler) {
        ChouKaObj result = new ChouKaObj(400, msg, null);
        CommUtil.getInstance().sendMessage(0, GsonUtil.toJson(result), handler);
    }
}
