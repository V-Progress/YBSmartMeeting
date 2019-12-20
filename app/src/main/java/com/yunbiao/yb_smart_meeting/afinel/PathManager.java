package com.yunbiao.yb_smart_meeting.afinel;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class PathManager {
    private static final String TAG = "PathManager";
    //生成本地地址
    public static String ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_meeting/";

    public static String DATA_PATH = "data/";//公司数据
    public static String INFO_PATH = "info/";//公司简介
    public static String ADS_PATH = "ads/";//广告路径
    public static String HEAD_PATH = "photo";//照片路径
    public static String FEATURE_PATH = "features";//照片路径
    public static String RECORD_PATH = "record/";//实时人脸记录缓存
    public static String MEETING_PATH = "meeting/";

    private static File companyFile = null;

    public static void init(int comId) {
        //创建公司目录
        companyFile = new File(ROOT_PATH,comId+"");
        if(companyFile == null || !companyFile.exists()){
            companyFile.mkdirs();
        }
        Log.e(TAG, "init: " + companyFile.getPath() + " --- " + companyFile.exists());


        //创建头像目录
        File headFile = new File(companyFile, HEAD_PATH);
        if(headFile == null || !headFile.exists()){
            headFile.mkdirs();
        }
        HEAD_PATH = headFile.getPath();
        Log.e(TAG, "init: " + HEAD_PATH + " --- " + headFile.exists());


        //创建特征目录
        File featureFile = new File(companyFile,FEATURE_PATH);
        if(featureFile == null || !featureFile.exists()){
            featureFile.mkdirs();
        }
        FEATURE_PATH = featureFile.getPath();
        Log.e(TAG, "init: " + FEATURE_PATH + " --- " + featureFile.exists());

        //创建广告目录
        File adsFile = new File(companyFile, ADS_PATH);
        if(adsFile == null || !adsFile.exists()){
            adsFile.mkdirs();
        }
        ADS_PATH = adsFile.getPath();
        Log.e(TAG, "init: " + ADS_PATH + " --- " + adsFile.exists());

    }
}
