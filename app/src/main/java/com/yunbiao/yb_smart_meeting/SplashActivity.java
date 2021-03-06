package com.yunbiao.yb_smart_meeting;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import com.yunbiao.yb_smart_meeting.activity.base.BaseActivity;
import com.yunbiao.yb_smart_meeting.utils.CommonUtils;
import com.yunbiao.yb_smart_meeting.utils.UIUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    public static String[] PERMISSONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE
            ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,android.Manifest.permission.ACCESS_FINE_LOCATION
            ,android.Manifest.permission.ACCESS_COARSE_LOCATION
            ,android.Manifest.permission.READ_PHONE_STATE
            ,android.Manifest.permission.CAMERA
            /*,Manifest.permission.SYSTEM_ALERT_WINDOW*/};
    private YBPermission ybPermission;
    private View ivBg;

    @Override
    protected String setTitle() {
        return null;
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        ivBg = findViewById(R.id.iv_bg);
    }

    @Override
    protected void initData() {
        // TODO: 2019/12/25 授权期限
       /* String overTime = "2020-01-07 00:00:00";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currDate = new Date();
        try {
            Date overDate = dateFormat.parse(overTime);
            if(currDate.after(overDate)){
                find(R.id.tv_guoqi).setVisibility(View.VISIBLE);
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        //开启看门狗,只会在开机是启动一次
        APP.bindProtectService();

        Config.deviceType = CommonUtils.getBroadType();
//        Config.deviceType = Config.DEVICE_MEETING;
        ybPermission = new YBPermission(new YBPermission.PermissionListener(){
            @Override
            public void onPermissionFailed(String[] objects) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(objects,YBPermission.PERMISSION_REQUEST_CODE);
                }
            }

            @Override
            public void onFinish(boolean isComplete) {
                if(isComplete){
                    APP.getContext().cauchException();
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, InitActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                } else {
                    UIUtils.showTitleTip(SplashActivity.this,"权限申请失败");
                }
            }
        });
        ybPermission.checkPermission(this,PERMISSONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ybPermission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public static class YBPermission{
        public static final int PERMISSION_REQUEST_CODE = 101;
        private PermissionListener permissionListener;
        public YBPermission(PermissionListener permissionListener) {
            this.permissionListener = permissionListener;
        }

        interface PermissionListener{
            void onPermissionFailed(String[] objects);
            void onFinish(boolean isComplete);
        }

        public void checkPermission(Activity activity,String[] permissions){
            if(permissions == null || permissions.length <= 0){
                if(permissionListener != null){
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> deniedPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissionList.add(permission);
                    }
                }
            }

            if(deniedPermissionList .size() > 0){
                String[] strings = deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(strings,PERMISSION_REQUEST_CODE);
                }
            } else {
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
            }
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults){
            if(requestCode != PERMISSION_REQUEST_CODE){
                return;
            }

            if (permissions == null || permissions.length<=0) {
                if(permissionListener != null){
                    permissionListener.onFinish(false);
                }
                return;
            }

            List<String> permiList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permiList.add(permissions[i]);
                }
            }
            if(permiList.size() <= 0){
                if(permissionListener != null){
                    permissionListener.onFinish(true);
                }
                return;
            }

            if(permissionListener != null){
                permissionListener.onPermissionFailed(permiList.toArray(new String[permiList.size()]));
            }
        }
    }
}
