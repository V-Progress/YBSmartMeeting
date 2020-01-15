package com.yunbiao.yb_smart_meeting.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2019/3/15.
 */

public class VipDialogManager {
    private static final String TAG = "VipDialogManager";
    private static Dialog vipDialog;

    private static VipDialogManager instance;

    private Activity mAct;

    public static VipDialogManager instance(){
        if(instance == null){
            synchronized(VipDialogManager.class){
                if(instance == null){
                    instance = new VipDialogManager();
                }
            }
        }
        return instance;
    }

    private VipDialogManager(){}

    public static void dismissVipDialog() {
        if (vipDialog != null && vipDialog.isShowing()) {
            vipDialog.dismiss();
        }
    }

    private static void init(Activity context) {
        if (vipDialog != null && vipDialog.isShowing()){
            vipDialog.dismiss();
            vipDialog = null;
        }

        vipDialog = new Dialog(context);
        //去掉标题线
        vipDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //背景透明
        vipDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Window window = vipDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }else {
            lp.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setAttributes(lp);
    }

    public static void showVipDialog(final Activity activity, final RecordInfo recordInfo){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init(activity);

                vipDialog.setContentView(R.layout.dialog_vip_item_test);
                ImageView ivHead = vipDialog.findViewById(R.id.civ_userPhoto);
                TextView tvName = vipDialog.findViewById(R.id.tv_nameAndJob);
                TextView tvSign = vipDialog.findViewById(R.id.tv_sign);

                Glide.with(activity).load(recordInfo.getHeadPath()).asBitmap().into(ivHead);
                tvName.setText(recordInfo.getName());

                if(!activity.isFinishing()){
                    vipDialog.show();
                }

                handler.removeMessages(0);
                handler.sendEmptyMessageDelayed(0,2000);
            }
        });
    }

    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dismissVipDialog();
        }
    };

}
