package com.yunbiao.yb_smart_meeting;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.faceview.Constants;
import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_meeting.activity.MeetingActivity;
import com.yunbiao.yb_smart_meeting.activity.WelComeActivity;
import com.yunbiao.yb_smart_meeting.afinel.PathManager;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.model.CompanyBean;
import com.yunbiao.yb_smart_meeting.business.DialogUtil;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class InitActivity extends AppCompatActivity {
    private static final String TAG = "InitActivity";
    private String deviceNumber;
    private String bindCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        EventBus.getDefault().register(this);

        deviceNumber = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        bindCode = SpUtils.getStr(SpUtils.BINDCODE);

        DialogUtil.showProgress(this, "正在登陆服务器...");
        //开启Xmpp
        APP.startXMPP();

        if (TextUtils.isEmpty(deviceNumber)) {
            return;
        }
        loadCompany();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(SysInfoUpdateEvent sysInfoUpdateEvent) {
        if (TextUtils.isEmpty(deviceNumber)) {
            deviceNumber = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
            bindCode = SpUtils.getStr(SpUtils.BINDCODE);
            loadCompany();
        }
    }

    private void loadCompany() {
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        map.put("deviceNo", deviceNo);
        Log.e(TAG, "地址: " + ResourceUpdate.COMPANYINFO);
        Log.e(TAG, "参数: " + map.toString());
        OkHttpUtils.post()
                .url(ResourceUpdate.COMPANYINFO)
                .params(map)
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    int result = -1;

                    @Override
                    public void onBefore(Request request, int id) {
                        DialogUtil.showProgress(InitActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onErrorGetMeeting: " + (e == null ? "NULL" : e.getMessage()));
                        result = -1;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: ----- " + response);
                        if (TextUtils.isEmpty(response)) {
                            result = -1;
                            return;
                        }

                        CompanyBean companyBean = new Gson().fromJson(response, CompanyBean.class);
                        result = companyBean.getStatus();
                        if (companyBean.getStatus() != 1) {
                            return;
                        }

                        int comid = companyBean.getCompany().getComid();
                        String name = companyBean.getCompany().getComname();
                        String pwd = companyBean.getCompany().getDevicePwd();
                        String logoUrl = companyBean.getCompany().getComlogo();

                        SpUtils.saveInt(SpUtils.COMPANY_ID, comid);
                        SpUtils.saveStr(SpUtils.COMPANY_NAME, name);
                        SpUtils.saveStr(SpUtils.MENU_PWD, pwd);
                        
                        SpUtils.saveStr(SpUtils.COMPANY_LOGO, logoUrl);
                    }

                    @Override
                    public void onAfter(int id) {
                        DialogUtil.dismissProgress(InitActivity.this);

                        if (result == 1) {
                            Log.e(TAG, "onAfter: 加载成功，跳转");
                            activeFaceSDK();
                        } else if (result == 4) {
                            Log.e(TAG, "onAfter: 未绑定公司，等待重试");
                            DialogUtil.showTimerAlertDialog(InitActivity.this, "编号：" + deviceNumber + "，绑定码：" + bindCode, "该设备未绑定公司，请先绑定", 60, retryRunnable);
                        } else {
                            int anInt = SpUtils.getInt(SpUtils.COMPANY_ID);
                            if (anInt > 0) {
                                Log.e(TAG, "onAfter: 有缓存，跳转");
                                activeFaceSDK();
                            } else {
                                DialogUtil.showTimerAlertDialog(InitActivity.this, "编号：" + deviceNumber + "，绑定码：" + bindCode, "请求失败，请检查网络", 30, retryRunnable);
                            }
                        }
                    }
                });
    }

    private void activeFaceSDK() {
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        PathManager.init(comId);

        int active = FaceEngine.active(APP.getContext(), Constants.APP_ID, Constants.SDK_KEY);
        if (active == ErrorInfo.MOK || active == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            Log.e(TAG, "激活成功或已激活");
            jump();
        } else {
            Toast.makeText(InitActivity.this, "激活失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void jump() {
        Intent intent = new Intent();

        double screenPhysicalSize = getScreenPhysicalSize(this);
        Log.e(TAG, "jump: 屏幕尺寸：" + screenPhysicalSize);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            intent.setClass(InitActivity.this, WelComeActivity.class);
        } else {
            intent.setClass(InitActivity.this, MeetingActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
    public static double getScreenPhysicalSize(Activity ctx) {
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
        return diagonalPixels / (160 * dm.density);
    }
    private Runnable retryRunnable = new Runnable() {
        @Override
        public void run() {
            loadCompany();
        }
    };
}
