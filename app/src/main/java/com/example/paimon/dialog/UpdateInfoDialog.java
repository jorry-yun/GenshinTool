package com.example.paimon.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.paimon.CommUtil;
import com.example.paimon.R;
import com.example.paimon.entity.AppUpdateMsg;

public class UpdateInfoDialog {

    private Context context;
    private AlertDialog.Builder builder;
    private OnCancelClickListener onCancelClickListener;
    private OnConfirmClickListener onConfirmClickListener;
    private AlertDialog dialog;


    private UpdateInfoDialog(Context context) {
        this.context = context;
    }

    public static UpdateInfoDialog getInstance(Context context) {
         return new UpdateInfoDialog(context);
    }

    public UpdateInfoDialog createDialog(AppUpdateMsg.UpdateHistory msg) {
        builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_update_info, null);
        TextView upVersion = view.findViewById(R.id.update_dialog_up_version);
        upVersion.setText(msg.getVersion());
        TextView currentVersion = view.findViewById(R.id.update_dialog_current_version);
        currentVersion.setText(CommUtil.getInstance().getVersionName(context));
        TextView size = view.findViewById(R.id.update_dialog_size);
        size.setText(msg.getSize());
        TextView date = view.findViewById(R.id.update_dialog_date);
        date.setText(msg.getDate());
        LinearLayout desc = view.findViewById(R.id.update_dialog_desc);
        for (String info : msg.getUpdateInfo()) {
            desc.addView(CommUtil.getInstance().generateTextView(context, null, info));
        }
        TextView confirm = view.findViewById(R.id.update_dialog_confirm);
        TextView cancel = view.findViewById(R.id.update_dialog_cancel);
        confirm.setOnClickListener(v -> {
            if (onConfirmClickListener != null) {
                onConfirmClickListener.onClick(v);
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

    public UpdateInfoDialog setOnConfirmClickListener(OnConfirmClickListener l) {
        onConfirmClickListener = l;
        return this;
    }

    public UpdateInfoDialog setOnCancelClickListener(OnCancelClickListener l) {
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
