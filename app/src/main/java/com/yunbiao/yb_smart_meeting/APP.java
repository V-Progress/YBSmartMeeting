package com.yunbiao.yb_smart_meeting;

import android.app.Activity;
import android.app.Application;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;


import com.android.xhapimanager.XHApiManager;
import com.bumptech.glide.Glide;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.yunbiao.yb_smart_meeting.activity.WelComeActivity;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.exception.CrashHandler2;
import com.yunbiao.yb_smart_meeting.receiver.MyProtectService;
import com.yunbiao.yb_smart_meeting.utils.RestartAPPTool;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xutils.x;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class APP extends Application {
    private static APP instance;
    private static SmdtManager smdt;
    private static int companyId;
    private static Activity activity;
    private static XHApiManager xhApiManager;
    private static ServiceManager serviceManager;

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        APP.activity = activity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initGpio();

        DaoManager.get().initDb();

        cauchException();

//        initBugly();
//
//        initUM();

        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection
                        .Configuration()
                        .connectTimeout(30000)
                        .readTimeout(30000)
                ))
                .commit();

        initHttp();
    }

    //IO引脚
    private int dir_set_io[] = {1, 2, 3, 4};
    //IO口方向，0：输入，1：输出
    private int dir_set_import = 0;
    private int dir_set_export = 1;
    //高低电平，0：低电平，1：高电平
    private int dir_set_value = 0;
    private static final String TAG = "APP";

    private void initGpio() {
        /*smdt = SmdtManager.create(this);
        //设置gpio为输出
        if (smdt != null) {
            for (int i = 0; i < dir_set_io.length; i++) {
                int dirToTemp = smdt.smdtSetGpioDirection(dir_set_io[i], dir_set_export, dir_set_value);
                if (dirToTemp == 0) {
                    Log.e(TAG, "initHttp: ----- 设置为输出成功");
                } else {
                    Log.e(TAG, "initHttp: ----- 设置为输出失败");
                }
            }
            return;
        }*/

//        try{
//            xhApiManager = new XHApiManager();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

    private void initHttp() {
        //初始化xutils 3.0
        x.Ext.init(this);

        OkHttpClient build = new OkHttpClient.Builder()
                .connectTimeout(60 * 1000, TimeUnit.SECONDS)
                .writeTimeout(60 * 1000, TimeUnit.SECONDS)
                .readTimeout(60 * 1000, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        OkHttpUtils.initClient(build);
    }

    // -------------------异常捕获-----捕获异常后重启系统-----------------//
    public void cauchException() {
        CrashHandler2.CrashUploader uploader = new CrashHandler2.CrashUploader() {
            @Override
            public void uploadCrashMessage(ConcurrentHashMap<String, Object> info, Throwable ex) {
                ex.printStackTrace();
                Log.e("APP", "uploadCrashMessage: -------------------");
//                CrashReport.postCatchedException(ex);
//                MobclickAgent.reportError(APP.getContext(), ex);

//                RestartAPPTool.restartAPP(APP.getContext());
            }
        };
        CrashHandler2.getInstance().init(this, uploader, null);
    }

    /*private void initUM() {
        UMConfigure.init(this, "5cbe87a60cafb210460006b3", "self", UMConfigure.DEVICE_TYPE_BOX, null);
        UMConfigure.setLogEnabled(false);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    private void initBugly() {
        // 获取当前包名
        String packageName = this.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        //设置渠道号
        strategy.setAppChannel("self");
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            @Override
            public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
                Log.e("APP", "onCrashHandleStart:11111111111111111111 ");
                return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack);
            }

            @Override
            public synchronized byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType, String errorMessage, String errorStack) {
                Log.e("APP", "onCrashHandleStart:22222222222222222222 ");
                return super.onCrashHandleStart2GetExtraDatas(crashType, errorType, errorMessage, errorStack);
            }
        });
        //设置用户ID
//        String deviceSernum = SpUtils.getString(APP.getContext(), SpUtils.DEVICE_NUMBER, "");
        String deviceSernum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        Bugly.setUserId(this, deviceSernum);
        // 初始化Bugly
        Bugly.init(this, "841dbdc324", false, strategy);

        //设置更新规则
        setUpgrade();
        //自动检测一次更新
        Beta.checkUpgrade(false, true);
    }

    private void setUpgrade() {
        *//**** Beta高级设置*****//*
        Beta.autoInit = true;//是否自动启动初始化
        Beta.autoCheckUpgrade = false;//是否自动检查升级
        Beta.initDelay = 1 * 1000;//检查周期
        Beta.largeIconId = R.mipmap.ic_launcher;//通知栏大图标
        Beta.smallIconId = R.mipmap.ic_launcher;//通知栏小图标
        Beta.defaultBannerId = R.mipmap.ic_launcher;
//        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//更新资源保存目录
        Beta.storageDir = new File(Constants.CACHE_PATH);//更新资源保存目录
        Beta.showInterruptedStrategy = false;//点击过确认的弹窗在APP下次启动自动检查更新时会再次显示
        Beta.autoDownloadOnWifi = true;//WIFI自动下载
        *//**
         * 自定义Activity参考，通过回调接口来跳转到你自定义的Actiivty中。
         *//*
        Beta.upgradeListener = new UpgradeListener() {
            @Override
            public void onUpgrade(int ret, UpgradeInfo strategy, boolean isManual, boolean isSilence) {
                if (strategy != null) {
                    Intent i = new Intent();
                    i.setClass(getApplicationContext(), WelComeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "没有更新", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Beta.registerDownloadListener(new DownloadListener() {
            @Override
            public void onReceive(DownloadTask downloadTask) {

            }

            @Override
            public void onCompleted(DownloadTask downloadTask) {
                File saveFile = downloadTask.getSaveFile();
                Log.e("APPPPP", "onCompleted: 1111111111111111 ----- " + saveFile + " --- " + saveFile.length());
            }

            @Override
            public void onFailed(DownloadTask downloadTask, int i, String s) {

            }
        });
    }*/

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public static APP getContext() {
        return instance;
    }

    public static SmdtManager getSmdt() {
        return smdt;
    }

    public static XHApiManager getXHApi(){
        return xhApiManager;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    public static void restart() {
        RestartAPPTool.restartAPP(getContext());
    }

    public static void startXMPP(){
        serviceManager = new ServiceManager(getContext());
        serviceManager.startService();
    }
    public static void stopXMPP(){
        serviceManager.stopService();
    }

    public static void bindProtectService(){
        //开启看门狗,只会在开机是启动一次
        getContext().startService(new Intent(APP.getContext(), MyProtectService.class));
    }

    public static void unbindProtectService(){
        getContext().stopService(new Intent(APP.getContext(), MyProtectService.class));
    }

    public static void exit() {
        unbindProtectService();
        //关闭整个应用
        System.exit(0);
    }
}