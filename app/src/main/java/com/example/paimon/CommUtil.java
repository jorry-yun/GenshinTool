package com.example.paimon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paimon.dialog.UpdateInfoDialog;
import com.example.paimon.entity.AppUpdateMsg;
import com.example.paimon.entity.WishVo;
import com.example.paimon.util.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommUtil {

    private final String CACHE_NAME = "paimon";

    private static CommUtil instance;

    public static CommUtil getInstance() {
        if (instance == null) {
            instance = new CommUtil();
        }
        return instance;
    }

    public SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CACHE_NAME, Context.MODE_PRIVATE);
    }

    public List<List<String>> splitList(List<String> list, int size) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i+=size) {
            List<String> subList = list.subList(i, Math.min(i + size, list.size()));
            if (!subList.isEmpty()) {
                result.add(subList);
            }
        }
        return result;
    }

    public void sendMessage(int what, String data, Handler handler) {
        Message msg = new Message();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public Map<String, String> parseUrl(String url) {
        Map<String, String> result = new HashMap<>();
        url = url.substring(url.indexOf("?") + 1);
        for (String s : url.split("&")) {
            if (s.contains("=")) {
                result.put(s.split("=")[0], s.split("=")[1]);
            }
        }
        return result;
    }

    public String toUrl(Map<String, String> resultMap) {
        StringBuilder url = new StringBuilder();
        for (String key : resultMap.keySet()) {
            if ("game_biz".equals(key)) {
                resultMap.put(key, "hk4e_cn");
            }
            url.append(key).append("=").append(resultMap.get(key)).append("&");
        }
        return url.substring(0, url.length() - 2);
    }

    public void writeCacheFile(Context context, String content, String fileName) {
        String path = context.getResources().getString(R.string.cache_path);
        try {
            path = getFileRoot(context) + path;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(e);
        }
    }

    public String readCacheFile(Context context, String fileName, String defaultValue) {
        String path = context.getResources().getString(R.string.cache_path);
        StringBuilder content = new StringBuilder();
        try {
            path = getFileRoot(context) + path;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path + fileName)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            Log.e(e);
        }
        String s = content.toString();
        return "".equals(s) ? defaultValue : s;
    }

    private static String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public int getCharacterColor(Map<String, String> data, String name) {
        Map<String, Integer> colorMap = new HashMap<>();
        colorMap.put("风", R.color.feng);
        colorMap.put("火", R.color.huo);
        colorMap.put("冰", R.color.bing);
        colorMap.put("水", R.color.shui);
        colorMap.put("雷", R.color.lei);
        colorMap.put("岩", R.color.yan);
        return 0;
    }

    public TextView generateTextView(Context context, Integer color, String text) {
        LinearLayout.LayoutParams etParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        etParam.setMargins(0, SystemUtil.Dp2Px(context, 10), SystemUtil.Dp2Px(context, 6), 0);
        // 单个角色
        TextView textView = generateNormalTextView(context, color, text);
        textView.setLayoutParams(etParam);
        return textView;
    }

    public TextView generateNormalTextView(Context context, Integer color, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        // 设置属性
        Resources resources = context.getResources();
        if (color != null) {
            textView.setTextColor(resources.getColor(color));
        }
        return textView;
    }

    public String getVersionName(Context context) {
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
        return null;
    }

    private Long downloadId;
    private boolean isRegisterReceiver = false;
    private void setReceiver(Context context) {
        if (!isRegisterReceiver) {
            DownloadReceiver receiver = new DownloadReceiver();
            DownloadReceiver.apkName = "genshinTool_1.2.apk";
            IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            context.registerReceiver(receiver, intentFilter);
            isRegisterReceiver = true;
        }
    }

    public void checkUpdate(Activity context, boolean autoIgnore) {
        String url = context.getResources().getString(R.string.url_app_update_msg);
        String currentVersion = getVersionName(context);
        if (currentVersion == null) {
            Toast.makeText(context, "获取当前版本失败", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences cache = getSharedPreferences(context);
        // 如果当前版本已经忽略更新过，则不提示更新
        if (autoIgnore && currentVersion.equals(cache.getString("ignore", ""))) {
            LinearLayout redPoint = context.findViewById(R.id.new_version);
            redPoint.removeAllViews();
            redPoint.addView(generateNormalTextView(context, R.color.huo, "●"));
            return;
        }
        new HttpUtil().get(url, AppUpdateMsg.class, new HttpCallBack<AppUpdateMsg>() {
            @Override
            public void onSuccess(AppUpdateMsg msg) {
                // 响应之后可再次点击
                context.findViewById(R.id.update).setClickable(true);
                AppUpdateMsg.UpdateHistory history = msg.getData().get(0);
                String upVersion = history.getVersion();
                // 服务器版本大于当前版本
                if (compareVersion(upVersion, currentVersion) > 0) {
                    UpdateInfoDialog dialog = UpdateInfoDialog.getInstance(context).createDialog(history);
                    dialog.setOnConfirmClickListener(view -> {
                        // 注册广播
                        setReceiver(context);
                        if (downloadId != null) {
                            HttpUtil.clearCurrentTask(context, downloadId);
                        }
                        downloadId = HttpUtil.download(context, history.getUrl());
                        Toast.makeText(context, "已开始下载", Toast.LENGTH_SHORT).show();
                    }).setOnCancelClickListener(view -> {
                        showDialog(context, "页面底部点击检查更新可再次进入哦");
                        // 忽略更新的版本号
                        cache.edit().putString("ignore", currentVersion).apply();
                    }).show();
                } else if (!autoIgnore){
                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Log.d(message);
                // 响应之后可再次点击
                context.findViewById(R.id.update).setClickable(true);
                Toast.makeText(context, "检查失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 版本号比较
     *
     * @return 0代表相等，1代表左边大，-1代表右边大
     * compareVersion("1.0.358_20180820090554","1.0.358_20180820090553")=1
     */
    public int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public void showDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("提示").setMessage(message)
                .setIcon(R.mipmap.favicon)
                .setPositiveButton("确定", (dialog, which) -> {})
                .create().show();
    }

}
