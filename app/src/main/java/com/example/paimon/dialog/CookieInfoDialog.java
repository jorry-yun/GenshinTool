package com.example.paimon.dialog;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paimon.CommUtil;
import com.example.paimon.R;
import com.example.paimon.entity.AppUpdateMsg;

public class CookieInfoDialog {

    private Context context;
    private AlertDialog.Builder builder;
    private OnCancelClickListener onCancelClickListener;
    private OnConfirmClickListener onConfirmClickListener;
    private AlertDialog dialog;


    private CookieInfoDialog(Context context) {
        this.context = context;
    }

    public static CookieInfoDialog getInstance(Context context) {
         return new CookieInfoDialog(context);
    }

    public CookieInfoDialog createDialog(String url) {
        builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cookie_info, null);
        TextView cookieInfoUrl = view.findViewById(R.id.cookie_info_url);
        cookieInfoUrl.setText(url);
        TextView confirm = view.findViewById(R.id.cookie_info_confirm);
        TextView cancel = view.findViewById(R.id.cookie_info_cancel);
        confirm.setOnClickListener(v -> {
            //获取剪切板管理器
            boolean success = CommUtil.getInstance().copyStr(context, url);
            if (success) {
                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(v -> {
            if (onCancelClickListener != null) {
                onCancelClickListener.onClick(v);
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.setCancelable(false);
        return this;
    }

    public CookieInfoDialog setOnConfirmClickListener(OnConfirmClickListener l) {
        onConfirmClickListener = l;
        return this;
    }

    public CookieInfoDialog setOnCancelClickListener(OnCancelClickListener l) {
        onCancelClickListener = l;
        return this;
    }

    public AlertDialog show() {
        dialog = builder.show();
        return dialog;
    }

    public interface OnCancelClickListener{
        void onClick(View v);
    }

    public interface OnConfirmClickListener{
        void onClick(View v);
    }
}
