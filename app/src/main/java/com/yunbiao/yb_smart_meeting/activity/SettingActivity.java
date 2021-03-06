package com.yunbiao.yb_smart_meeting.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.Config;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.GpioEvent;
import com.yunbiao.yb_smart_meeting.activity.base.BaseActivity;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.common.UpdateVersionControl;
import com.yunbiao.yb_smart_meeting.faceview.camera.CameraSettings;
import com.yunbiao.yb_smart_meeting.faceview.camera.ExtCameraManager;
import com.yunbiao.yb_smart_meeting.faceview.rect.FaceBoxUtil;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    private TextView tvNetState;
    private TextView tvCpuTemper;
    private TextView tvCamera;
    private Switch swAlready;

    @Override
    protected String setTitle() {
        return "系统设置";
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        tvNetState = findViewById(R.id.tv_wifi_state);
        tvCpuTemper = findViewById(R.id.tv_cpu_temper);
        tvCamera = findViewById(R.id.tv_camera);
        swAlready = findViewById(R.id.sw_setting_already);
        findViewById(R.id.iv_back).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    finish();
                }
                return false;
            }
        });

        initSetting();
    }

    private void initSetting(){
        //门禁常开
        boolean aBoolean = SpUtils.getBoolean(SpUtils.DOOR_STATE, false);
        swAlready.setChecked(aBoolean);
        swAlready.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EventBus.getDefault().postSticky(new GpioEvent(isChecked));
                SpUtils.saveBoolean(SpUtils.DOOR_STATE,isChecked);
            }
        });

        //横向镜像开关
        Switch swH = findViewById(R.id.sw_h_mirror);
        boolean hMirror = SpUtils.getBoolean(Constants.Key.FACE_H_MIRROR, Constants.Default.FACE_H_MIRROR);
        swH.setChecked(hMirror);
        swH.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpUtils.saveBoolean(Constants.Key.FACE_H_MIRROR,isChecked);
        });

        //纵向开关
        Switch swV = findViewById(R.id.sw_v_mirror);
        boolean vMirror = SpUtils.getBoolean(Constants.Key.FACE_V_MIRROR, Constants.Default.FACE_V_MIRROR);
        swV.setChecked(vMirror);
        swV.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SpUtils.saveBoolean(Constants.Key.FACE_V_MIRROR,isChecked);
        });

        int similarThreshold = SpUtils.getIntOrDef(Constants.Key.SIMILAR_THRESHOLD,Constants.Default.SIMILAR_THRESHOLD);
        EditText edtSimilar = findViewById(R.id.edt_similar_threshold);
        findViewById(R.id.btn_set_similar_threshold).setOnClickListener(v -> {
            String inputSimilar = edtSimilar.getText().toString();
            if(TextUtils.isEmpty(inputSimilar)){
                edtSimilar.setText(similarThreshold + "");
                return;
            }

            int i = Integer.parseInt(inputSimilar);
            SpUtils.saveInt(Constants.Key.SIMILAR_THRESHOLD,i);
            edtSimilar.setText(similarThreshold + "");
        });

        Switch swLive = findViewById(R.id.sw_live);
        boolean liveEnabled = SpUtils.getBoolean(Constants.Key.LIVE_ENABLED, Constants.Default.LIVE_ENABLED);
        swLive.setChecked(liveEnabled);

        Button btnAngle = findViewById(R.id.btn_setAngle);
        int cameraAngle = SpUtils.getIntOrDef(Constants.Key.RGB_CAMERA_ANGLE,Constants.Default.RGB_CAMERA_ANGLE);
        btnAngle.setText( "角度：" + cameraAngle);
        btnAngle.setOnClickListener(v -> {
            int angle = SpUtils.getIntOrDef(Constants.Key.RGB_CAMERA_ANGLE,Constants.Default.RGB_CAMERA_ANGLE);
            switch (angle) {
                case 0:
                    angle = 90;
                    break;
                case 90:
                    angle = 180;
                    break;
                case 180:
                    angle = 270;
                    break;
                case 270:
                default:
                    angle = 0;
                    break;
            }
            SpUtils.saveInt(Constants.Key.RGB_CAMERA_ANGLE,angle);
            btnAngle.setText( "角度：" + angle);
            EventBus.getDefault().post(new DisplayOrientationEvent());
        });
    }

    @Override
    protected void initData() {
        //设置网络
        checkNet();

        //获取CPU温度
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String s = CpuUtils.getCpuTemperatureFinder() + "℃";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCpuTemper.setText(s);
                    }
                });
            }
        },0, 3 , TimeUnit.SECONDS);

        //摄像头模式
        tvCamera.setText("【" + (Config.getCameraType() == Config.CAMERA_AUTO ? "自动" : Config.getCameraType() == Config.CAMERA_BACK? "后置" : "前置") + "，分辨率：" + CameraSettings.getCameraPreviewWidth()+"*" + CameraSettings.getCameraPreviewHeight() + "】" );

        setListSize();
    }

    class SizeBean{
        int width;
        int height;
        String desc;
    }

    private void setListSize(){
        List<Camera.Size> supportSizeList = ExtCameraManager.instance().getSupportSizeList();
        if (supportSizeList == null) {
            return;
        }
        Collections.sort(supportSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if(o1.width > o2.width){
                    return -1;
                }

                if(o1.width == o2.width){
                    if(o1.height > o2.height){
                        return -1;
                    }
                    return 0;
                }
                return 1;
            }
        });


        final List<SizeBean> sizeBeanList = new ArrayList<>();
        for (Camera.Size size : supportSizeList) {
            SizeBean sizeBean = new SizeBean();
            sizeBean.width = size.width;
            sizeBean.height = size.height;
            float i = (float) size.width / (float) size.height;

            if(i < 1.6){
                if(i > 1.3){
                    sizeBean.desc = "（4 : 3）";
                } else {
                    sizeBean.desc = "（5 : 4）";
                }
            } else {
                sizeBean.desc = "（16 : 9）";
            }

            if(sizeBean.width == 1280 && sizeBean.height == 720){
                sizeBean.desc += "最佳";
            } else if(sizeBean.width == 1920 && sizeBean.height == 1080){
                sizeBean.desc += "最大";
            }
            sizeBeanList.add(sizeBean);
        }

        int cameraWidth = CameraSettings.getCameraWidth();
        int cameraHeight = CameraSettings.getCameraHeight();

        int index = 0;
        for (int i = 0; i < sizeBeanList.size(); i++) {
            SizeBean sizeBean = sizeBeanList.get(i);
            if(cameraWidth == sizeBean.width && cameraHeight == sizeBean.height){
                index = i;
            }
        }

        /*spnCameraSize.setAdapter(new SizeAdapter(sizeBeanList));
        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnCameraSize.setPopupBackgroundDrawable(drawable);
        spnCameraSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                SizeBean sizeBean = sizeBeanList.get(position);
                CameraSettings.setCameraPreviewWidth(sizeBean.width);
                CameraSettings.setCameraPreviewHeight(sizeBean.height);
                SpUtils.saveInt(SpUtils.CAMERA_WIDTH,sizeBean.width);
                SpUtils.saveInt(SpUtils.CAMERA_HEIGHT,sizeBean.height);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnCameraSize.setSelection(index);*/
    }

    class SizeAdapter extends BaseAdapter{
        List<SizeBean> sizeBeanList;
        public SizeAdapter(List<SizeBean> sizeBeanList) {
            this.sizeBeanList = sizeBeanList;
        }

        @Override
        public int getCount() {
            return sizeBeanList.size();
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView inflate = (TextView) View.inflate(SettingActivity.this, android.R.layout.simple_dropdown_item_1line, null);
            inflate.setTextColor(Color.parseColor("#59B4CA"));
            inflate.setTextSize(16);
            inflate.setPadding(15,15,15,15);
            SizeBean sizeBean = sizeBeanList.get(position);
            inflate.setText(sizeBean.width + " * " + sizeBean.height + " " + sizeBean.desc);
            return inflate;
        }
    }

    public void modifyPWD(View view) {
        setPwd();
    }

    public void checkUpgrade(View view) {
        UpdateVersionControl.getInstance().checkUpdate(this);
    }

    public void hardwareTest(View view) {
        UIUtils.showTitleTip(this,"正在开发中！");
    }

    /**
     * ====功能区==================================================================================================
     */
    /*public void setAngle(final View view) {
        int anInt = SpUtils.getInt(SpUtils.CAMERA_ANGLE);
        if(anInt == CameraSettings.ROTATION_0){
            anInt = CameraSettings.ROTATION_90;
        } else if(anInt == CameraSettings.ROTATION_90) {
            anInt = CameraSettings.ROTATION_180;
        } else if(anInt == CameraSettings.ROTATION_180){
            anInt = CameraSettings.ROTATION_270;
        } else {
            anInt = CameraSettings.ROTATION_0;
        }
        ((Button)view).setText("角度：" + anInt);
        SpUtils.saveInt(SpUtils.CAMERA_ANGLE, anInt);
        CameraSettings.setCameraDisplayRotation(anInt);
    }*/

    public void rebootDevice(View view) {
        showAlert("设备将重启，是否继续？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(SettingActivity.this);
                progressDialog.setTitle("重启");
                progressDialog.setMessage("3秒后将重启设备");
                progressDialog.setCancelable(false);
                progressDialog.show();
                UIUtils.restart.start();
            }
        },null, null);
    }

    private void checkNet() {
        String net = "";
        boolean intenetConnected = isIntenetConnected(this);
        if (intenetConnected) {
            net = "【以太网络，IP地址：" + getHostIp() + "】";
        } else {
            net = "【WIFI，" + getWifiInfo(0) + "，IP地址：" + getWifiInfo(1) + "】";
        }
        tvNetState.setText(net);
    }

    private String getHostIp() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    public static String getWifiInfo(int type) {
        WifiManager wifiManager = (WifiManager) APP.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }

        WifiInfo wi = wifiManager.getConnectionInfo();
        Log.e(TAG, "getWifiInfo() wi=" + wi);
        if (wi == null) {
            return null;
        }
        if(type == 0){
            return "名称：" +wi.getSSID() + "，信号强度："+wi.getRssi();
        }

        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        Log.e(TAG, "getWifiInfo() ipAdd=" + ipAdd);
        if (ipAdd == 0) {
            return null;
        }
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        Log.e(TAG, "getWifiInfo() ip=" + ip);

        if (ip == null || ip.startsWith("0")) {
            return null;
        }
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private static boolean isIntenetConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
            return hasInternet;
        }
        return false;
    }

    /**
     * 判断对象是否为空
     *
     * @param object
     * @return
     */
    public static boolean isNullObject(Object object) {

        if (object == null) {
            return true;
        }

        return false;
    }

    private void showAlert(String msg, Dialog.OnClickListener onClickListener, Dialog.OnClickListener onCancel ,DialogInterface.OnDismissListener onDissmissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", onClickListener);
        builder.setNegativeButton("取消", onCancel);
        if (onDissmissListener != null) {
            builder.setOnDismissListener(onDissmissListener);
        }

        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        alertDialog.show();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
    }

    public void setPwd() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_set_pwd);

        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_set_pwd);
        final EditText edtPwd2 = (EditText) dialog.findViewById(R.id.edt_set_pwd_again);
        final Button btnCancel = (Button) dialog.findViewById(R.id.btn_pwd_cancel);
        final Button btnConfirm = (Button) dialog.findViewById(R.id.btn_pwd_confirm);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtPwd.getText())) {
                    edtPwd.setError("密码不可为空");
                    return;
                }
                if (edtPwd.getText().length() < 6) {
                    edtPwd.setError("密码最少输入6位");
                    return;
                }
                if (TextUtils.isEmpty(edtPwd2.getText())) {
                    edtPwd2.setError("请再次输入密码");
                    return;
                }
                String pwd = edtPwd.getText().toString();
                final String pwd2 = edtPwd2.getText().toString();
                if (!TextUtils.equals(pwd, pwd2)) {
                    edtPwd2.setError("两次输入的密码不一致");
                    return;
                }

                btnCancel.setEnabled(false);
                btnConfirm.setEnabled(false);
                Map<String, String> params = new HashMap<>();
                params.put("deviceNo", HeartBeatClient.getDeviceNo());
                params.put("password", pwd2);
                OkHttpUtils.post().url(ResourceUpdate.UPDATE_PWD).params(params).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int id) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UIUtils.showTitleTip(SettingActivity.this,"修改失败：" + e != null ? e.getMessage() : "NULL");
                            }
                        });
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        final Integer status = jsonObject.getInteger("status");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == 1) {
                                    UIUtils.showTitleTip(SettingActivity.this,"修改成功");
                                    SpUtils.saveStr(SpUtils.MENU_PWD, pwd2);
                                    dialog.dismiss();
                                } else {
                                    UIUtils.showTitleTip(SettingActivity.this,"修改失败");
                                }
                            }
                        });
                    }

                    @Override
                    public void onAfter(int id) {
                        btnConfirm.setEnabled(true);
                        btnCancel.setEnabled(true);
                    }
                });
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }


    public static class CpuUtils {
        private CpuUtils() {
            //no instance
        }
        private static final List<String> CPU_TEMP_FILE_PATHS = Arrays.asList(
                "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
                "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
                "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
                "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
                "/sys/devices/platform/tegra_tmon/temp1_input",
                "/sys/kernel/debug/tegra_thermal/temp_tj",
                "/sys/devices/platform/s5p-tmu/temperature",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/hwmon/hwmon0/device/temp1_input",
                "/sys/devices/virtual/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone3/temp",
                "/sys/class/thermal/thermal_zone4/temp",
                "/sys/class/hwmon/hwmonX/temp1_input",
                "/sys/devices/platform/s5p-tmu/curr_temp");

        public static final String getCpuTemperatureFinder() {
            String currTemp = "-1";
            for (String cpuTempFilePath : CPU_TEMP_FILE_PATHS) {
                Double temp = readOneLine(new File(cpuTempFilePath));
                String validPath = "";
                double currentTemp = 0.0D;
                if (isTemperatureValid(temp)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp;
                } else if (isTemperatureValid(temp / (double) 1000)) {
                    validPath = cpuTempFilePath;
                    currentTemp = temp / (double) 1000;
                }

                if(!TextUtils.isEmpty(validPath)
                        && (currentTemp != 0)){
                    currTemp = currentTemp+"";
                }
            }
            return currTemp;
        }

        private static double readOneLine(File file) {
            FileInputStream fileInputStream = null;
            String s = "";
            try {
                fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                s = bufferedReader.readLine();
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            } catch (IOException e) {
            }

            double result = 0;
            try {
                result = Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
            return result;
        }

        private static boolean isTemperatureValid(double temp) {
            return temp >= -30.0D && temp <= 250.0D;
        }
    }

    class CheckCamera{
        public String getCameraInfo(){
            StringBuilder cameraInfo = new StringBuilder();
            int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
            if(numberOfCameras <= 0){
                return "无摄像头";
            }
            for (int i = 0; i < numberOfCameras; i++) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(i,info);
                boolean isFront = info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
                int orientation = info.orientation;
                cameraInfo
                        .append("共"+numberOfCameras+"个：")
                        .append("【编号："+i+"，")
                        .append(isFront ? "前置":"后置")
                        .append("，角度："+orientation)
                        .append("】");
            }

            return cameraInfo.toString();
        }
    }

    class CheckNet {
        private  WifiManager wifiManager;
        private  ConnectivityManager connectManager;
        private Context context;

        public CheckNet(Context context) {
            this.context = context;
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        /**
         * 判断以太网网络是否可用
         *
         * @return
         */
        public boolean isIntenetConnected() {
            if (context != null) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mInternetNetWorkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                boolean hasInternet = !isNullObject(mInternetNetWorkInfo) && mInternetNetWorkInfo.isConnected() && mInternetNetWorkInfo.isAvailable();
                return hasInternet;
            }
            return false;
        }

        /**
         * 判断对象是否为空
         *
         * @param object
         * @return
         */
        private boolean isNullObject(Object object) {
            return object == null;
        }

        //获取wifi状态
        public boolean isWifiEnabled() {
            int wifiState = wifiManager.getWifiState();
            return wifiState == WifiManager.WIFI_STATE_ENABLED;
        }

        public boolean isWifiConnected() {
            //wifi连接
            NetworkInfo info = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return info.isConnected();
        }

        public boolean isEtherneteConncted(){
            NetworkInfo info = connectManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            return info.isConnected();
        }

        //获取wifi名称
        public String getWifiName() {
            WifiInfo info = wifiManager.getConnectionInfo();
            if ((info != null) && (!TextUtils.isEmpty(info.getSSID()))) {
                return info.getSSID();
            }
            return "NULL";
        }

        public String getStrength(){
            String strength = "";
            WifiInfo info = wifiManager.getConnectionInfo();
            int rssi = info.getRssi();
            if(rssi<=0 && rssi >= -50){//信号最好
                strength = "强";
            }else if(rssi< -50 && rssi >= -70){//信号一般
                strength = "一般";
            }else if(rssi > -70){
                strength = "差";
            }else if(rssi <= -200){
                strength = "无网络";
            }
            return strength;
        }
    }
}
