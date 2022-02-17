package com.example.paimon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.paimon.entity.WishVo;
import com.example.paimon.util.GsonUtil;
import com.example.paimon.util.Log;
import com.example.paimon.util.SystemUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommUtil {

    private static CommUtil instance;

    public static CommUtil getInstance() {
        if (instance == null) {
            instance = new CommUtil();
        }
        return instance;
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
}
