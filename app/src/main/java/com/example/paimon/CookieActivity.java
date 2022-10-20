package com.example.paimon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;

import com.example.paimon.dialog.CookieInfoDialog;
import com.example.paimon.entity.ChouKaObj;
import com.example.paimon.entity.ListUrl;
import com.example.paimon.util.AuthKeyUtil;
import com.example.paimon.util.GsonUtil;
import com.example.paimon.util.Log;
import com.example.paimon.util.StringUtil;

import java.util.List;

public class CookieActivity extends AppCompatActivity {

    private final Handler handler3 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(GsonUtil.toJson(msg));
            ChouKaObj result = GsonUtil.parseJson(msg.getData().getString("data"), ChouKaObj.class);
            if (result.getCode() == 200) {
                List<ListUrl> urlList = result.getUrlListObj();
                if (urlList != null && !urlList.isEmpty()) {
                    ListUrl listUrl = urlList.get(0);
                    CookieInfoDialog.getInstance(CookieActivity.this).createDialog(listUrl.getUrl()).show();
                } else {
                    Toast.makeText(CookieActivity.this, "cookie有效，但似乎未绑定账号", Toast.LENGTH_SHORT).show();
                }
            }
            if (result.getCode() == 400) {
                Toast.makeText(CookieActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie);
        EditText cookieEdit = findViewById(R.id.activity_cookie_content);
        findViewById(R.id.activity_cookie_generate).setOnClickListener((view) -> {
            String cookie = cookieEdit.getText().toString();
            if (StringUtil.isBlank(cookie)) {
                Toast.makeText(this, "请粘贴cookie", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthKeyUtil.getInstance().getAuthkey(cookie, handler3);
        });
    }
}