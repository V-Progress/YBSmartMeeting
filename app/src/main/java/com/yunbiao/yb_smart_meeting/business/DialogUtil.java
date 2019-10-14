package com.yunbiao.yb_smart_meeting.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class DialogUtil {
    private static final String DEFAULT_MESSAGE = "正在加载...";
    private static final String DEFAULT_TITLE = "提示";
    private static ProgressDialog progressDialog;
    private static AlertDialog.Builder builder;
    private static AlertDialog alertDialog;

    private static Handler handler = new Handler();
    private static Runnable runnable;

    /***
     * 加载条
     * @param context
     */
    public static void showProgress(Activity context) {
        showProgress(context, null);
    }

    public static void showProgress(final Activity context, final String msg) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tempMsg = msg;
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                if (TextUtils.isEmpty(tempMsg)) {
                    tempMsg = DEFAULT_MESSAGE;
                }
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(tempMsg);
                progressDialog.show();
            }
        });

    }

    public static void dismissProgress(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }

    /***
     * 警告提示
     * @param context
     * @param msg
     * @return
     */
    public static AlertDialog showAlert(Activity context, String msg) {
        return showAlert(context, null, msg);
    }

    public static AlertDialog showAlert(Activity context, String title, String msg) {
        if (builder == null) {
            builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }

        alertDialog = builder.create();
        alertDialog.setTitle(TextUtils.isEmpty(title) ? DEFAULT_TITLE : title);
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(msg);
        alertDialog.show();
        return alertDialog;
    }

    public void dismissAlert() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public static void showTimerAlertDialog(Activity context, String title, final String msg, final int finishTime, final Runnable finishRunnable) {
        final AlertDialog alertDialog = showAlert(context, title,msg + "（" + finishTime + "秒后重试）");
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            int time = finishTime;

            @Override
            public void run() {
                if (time <= 1) {
                    finishRunnable.run();
                    alertDialog.dismiss();
                    return;
                }
                time--;
                alertDialog.setMessage(msg + "（" + time + "秒后重试）");
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }
}
