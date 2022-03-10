package com.example.paimon.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import com.example.paimon.entity.RequestResult;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class HttpUtil {
//    private String authKey;

//    private HttpUtil(Context mContext) {
//        MyApplication app = null;
//        if (mContext instanceof Activity) {
//            Activity activity = (Activity) mContext;
//            app = (MyApplication) activity.getApplication();
//        }
//        if (mContext instanceof Service) {
//            Service service = (Service) mContext;
//            app = (MyApplication) service.getApplication();
//        }
//        if (app != null) {
//            authKey = app.getAuthKey();
//        }
//    }

//    public static HttpUtil getInstance(Context mContext) {
//        return new HttpUtil(mContext);
//    }

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_FROM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static OkHttpClient okHttpClient;
    private static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            synchronized (HttpUtil.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)//10秒连接超时
                            .writeTimeout(10, TimeUnit.SECONDS)//10m秒写入超时
                            .readTimeout(10, TimeUnit.SECONDS)//10秒读取超时
                            //.addInterceptor(new HttpHeaderInterceptor())//头部信息统一处理
                            //.addInterceptor(new CommonParamsInterceptor())//公共参数统一处理
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    /**
     * @param url      url地址
     * @param callBack 请求回调接口
     */
    public void get(String url, HttpCallBack callBack) {
        commonGet(getRequestForGet(url, null, null), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param cls      泛型返回参数
     * @param callBack 请求回调接口
     */
    public <T> void get(String url, Class<T> cls, HttpCallBack<T> callBack) {
        commonGet(getRequestForGet(url, null, null), cls, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, String> 参数
     * @param callBack 请求回调接口
     */
    public void get(String url, Map<String, String> params, HttpCallBack callBack) {
        commonGet(getRequestForGet(url, params, null), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, String> 参数
     * @param cls      泛型返回参数
     * @param callBack 请求回调接口
     */
    public <T> void get(String url, Map<String, String> params, Class<T> cls, HttpCallBack<T> callBack) {
        commonGet(getRequestForGet(url, params, null), cls, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, String> 参数
     * @param callBack 请求回调接口
     * @param tag      网络请求tag
     */
    public void get(String url, Map<String, String> params, HttpCallBack callBack, Object tag) {
        commonGet(getRequestForGet(url, params, tag), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, String> 参数
     * @param callBack 请求回调接口
     * @param cls      泛型返回参数
     * @param tag      网络请求tag
     */
    public <T> void get(String url, Map<String, String> params, Class<T> cls, HttpCallBack<T> callBack, Object tag) {
        commonGet(getRequestForGet(url, params, tag), cls, callBack);
    }


    /**
     * @param url      url地址
     * @param callBack 请求回调接口
     */
    public void post(String url, HttpCallBack callBack) {
        commonPost(getRequestForPost(url, null, null), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param cls      泛型返回参数
     * @param callBack 请求回调接口
     */
    public <T> void post(String url, Class<T> cls, HttpCallBack<T> callBack) {
        commonPost(getRequestForPost(url, null, null), cls, callBack);
    }


    /**
     * @param url      url地址
     * @param params   Map<String, Object> 参数
     * @param callBack 请求回调接口
     */
    public void post(String url, Map<String, Object> params, HttpCallBack callBack) {
        commonPost(getRequestForPost(url, params, null), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, Object> 参数
     * @param callBack 请求回调接口
     * @param tag      网络请求tag
     */
    public void post(String url, Map<String, Object> params, HttpCallBack callBack, Object tag) {
        commonPost(getRequestForPost(url, params, tag), RequestResult.class, callBack);
    }

    /**
     * @param url      url地址
     * @param params   Map<String, Object> 参数
     * @param cls      泛型返回参数
     * @param callBack 请求回调接口
     */
    public <T> void post(String url, Map<String, Object> params, Class<T> cls, final HttpCallBack<T> callBack) {
        commonPost(getRequestForPost(url, params, null), cls, callBack);
    }

    /**
     * GET请求 公共请求部分
     */
    private <T> void commonGet(Request request, final Class<T> cls, final HttpCallBack<T> callBack) {
        if (request == null) return;
        Call call = getInstance().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                try {
                    if (callBack != null && mainHandler != null) {
                        e.printStackTrace();
                        mainHandler.post(() -> callBack.onFailure(e.getMessage()));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Log.e(StringUtil.TAG, "HttpUtil----commonGet()---onFailure()--->" + e.getMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String result = response.body().string();
                    if (result.startsWith("callback(")) {
                        result = result.replace("callback(", "");
                        result = result.substring(0, result.length() - 1);
                    }
                    final T json = GsonUtil.parseJson(result, cls);
                    if (callBack != null && mainHandler != null && json != null) {
                        mainHandler.post(() -> callBack.onSuccess(json));
                    } else {
                        Log.e(StringUtil.TAG, "HttpUtil----commonGet()---onResponse()--->" + json.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(StringUtil.TAG, "HttpUtil----commonGet()---onResponse()--->" + e.getMessage());
                }
            }
        });
    }

    /**
     * POST请求 公共请求部分
     */
    private <T> void commonPost(Request request, final Class<T> cls, final HttpCallBack<T> callBack) {
        if (request == null) return;
        Call call = getInstance().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                try {
                    if (callBack != null && mainHandler != null) {
                        mainHandler.post(() -> callBack.onFailure(e.getMessage()));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Log.e(StringUtil.TAG, "HttpUtil----commonPost()---onFailure()--->" + e1.getMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (callBack != null && mainHandler != null) {
                        final T json = GsonUtil.parseJson(response.body().string(), cls);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(json);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(StringUtil.TAG, "HttpUtil----commonPost()---onResponse()--->" + e.getMessage());
                }
            }

        });
    }

    private Request getRequestForPost(String url, Map<String, Object> params, Object tag) {
        if (url == null || "".equals(url)) {
            Log.e(StringUtil.TAG, "HttpUtil----getRequestForPost()---->" + "url地址为空 无法执行网络请求!!!");
            return null;
        }
//        if (url.contains("?")) {
//            url += "&key=" + authKey;
//        }else {
//            url += "?key=" + authKey;
//        }
        if (params == null) {
            params = new HashMap<>();
        }
//        //判断用户是否登录 登录带上用户基础参数 如只需要userId token接口无需传参数
//        if (UserManager.getIsLogined()) {
//            params.put("userId", UserManager.getUserId());
//            params.put("token", UserManager.getToken());
//        }
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, GsonUtil.toJson(params));
        Request request;
        Request.Builder post = new Request.Builder().url(url).post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9) Gecko/20080705 Firefox/3.0 Kapiko/3.0")
                .addHeader("Referer", "https://music.163.com/");
        if (tag != null) {
            request = post.tag(tag).build();
        } else {
            request = post.build();
        }
        return request;
    }

    private Request getRequestForGet(String url, Map<String, String> params, Object tag) {
        if (url == null || "".equals(url)) {
            Log.e(StringUtil.TAG, "HttpUtil----getRequestForGet()---->" + "url地址为空 无法执行网络请求!!!");
            return null;
        }
//        if (url.contains("?")) {
//            url += "&key=" + authKey;
//        }else {
//            url += "?key=" + authKey;
//        }
        Request request;
        if (tag != null) {
            request = new Request.Builder()
                    .url(paramsToString(url, params))
                    .tag(tag)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(paramsToString(url, params))
                    .build();
        }
        return request;
    }

    private String paramsToString(String url, Map<String, String> params) {
        StringBuilder url_builder = new StringBuilder();
        url_builder.append(url);
        if (params != null && params.size() > 0) {
            url_builder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    url_builder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
                } catch (Exception e) {
                    e.printStackTrace();
                    url_builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            url_builder = url_builder.deleteCharAt(url_builder.length() - 1);
        }
        return url_builder.toString();
    }

    /**
     * 根据tag标签取消网络请求
     */
    public void cancelTag(Object tag) {
        if (tag == null) return;
        for (Call call : getInstance().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getInstance().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }


    /**
     * 取消所有请求请求
     */
    public void cancelAll() {
        for (Call call : getInstance().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getInstance().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    /**
     * 比较实用的升级版下载功能
     *
     * @param url   下载地址
     */
    public static long download(Context mContext, String url) {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        long ID;
        String apkName = url.substring(url.lastIndexOf("/") + 1);

        //以下两行代码可以让下载的apk文件被直接安装而不用使用Fileprovider,系统7.0或者以上才启动。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder localBuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(localBuilder.build());
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 仅允许在WIFI连接情况下下载
//        request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
        // 通知栏中将出现的内容
        request.setTitle(apkName);
        request.setDescription("下载安装包");

        //7.0以上的系统适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.setRequiresDeviceIdle(false);
            request.setRequiresCharging(false);
        }

        //制定下载的文件类型为APK
        request.setMimeType("application/vnd.android.package-archive");

        // 下载过程和下载完成后通知栏有通知消息。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 指定下载文件地址，使用这个指定地址可不需要WRITE_EXTERNAL_STORAGE权限。
        request.setDestinationInExternalPublicDir("Download", apkName);
        //大于11版本手机允许扫描
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //表示允许MediaScanner扫描到这个文件，默认不允许。
            request.allowScanningByMediaScanner();
        }

        ID = downloadManager.enqueue(request);
        return ID;
    }

    /**
     * 下载前先移除前一个任务，防止重复下载
     */
    public static void clearCurrentTask(Context mContext, long downloadId) {
        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            dm.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

}