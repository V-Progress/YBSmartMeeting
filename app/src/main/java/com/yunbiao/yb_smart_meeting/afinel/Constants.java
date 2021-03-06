package com.yunbiao.yb_smart_meeting.afinel;

import android.os.Environment;

import com.yunbiao.yb_smart_meeting.BuildConfig;

public class Constants {
    public static final String API_KEY = "1234567890";

    //天津港
    public static final String XMPP_HOST = BuildConfig.HOST;
    public static final String XMPP_PORT = BuildConfig.XMPP_PORT;
    public static final String RESOURCE_HOST = NetConfig.PRE + BuildConfig.HOST;
    public static final String RESOURCE_PORT = BuildConfig.RESOURCE_PORT;
    //生成主地址
    public static String RESOURCE_URL = RESOURCE_HOST + NetConfig.COLON + RESOURCE_PORT + BuildConfig.SUFFIX;

    //生成本地地址
    public static String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/yb_meeting/";
    public static String DATABASE_PATH = LOCAL_ROOT_PATH + "database/";//数据库路径
    public static String CACHE_PATH = LOCAL_ROOT_PATH + "cache/";//缓存路径

    public static String TEMP_PATH = CACHE_PATH + "temp/";//临时路径（未初始化公司时创建）
    public static String DATA_PATH = TEMP_PATH + "data/";//公司数据
    public static String INFO_PATH = TEMP_PATH + "info/";//公司简介
    public static String ADS_PATH = TEMP_PATH + "ads/";//广告路径
    public static String HEAD_PATH = TEMP_PATH + "photo/";//照片路径
    public static String FEATURE_PATH = TEMP_PATH + "features/";//照片路径
    public static String RECORD_PATH = TEMP_PATH + "record/";//实时人脸记录缓存
    public static String MEETING_PATH = TEMP_PATH + "meeting/";

    public final static String DEFALUT_TIPS = "识别成功 na 欢迎光临";
    public final static String DEFALUT_LEADER_TIPS = "欢迎 na po 第s次莅临指导";

    public interface NetConfig{
        String PRE = "http://";
        String COLON = ":";

        /***
         * 正式环境
         */
//        String PRO_URL = "47.105.80.245";
//        String PRO_RES_PORT = "8080";
//        String PRO_XMPP_PORT = "5222";
//        String PRO_SUFFIX = "/";

        /***
         * 张继桃环境
         */
//        String DEV_URL = "192.168.1.54";
//        String DEV_RES_PORT = "8088";
//        String DEV_XMPP_PORT = "5222";
//        String DEV_SUFFIX = "/ybface/";
        /***
         * 正式环境
         */
        String DEV_URL = "47.105.80.245";
        String DEV_RES_PORT = "8080";
        String DEV_XMPP_PORT = "5222";
        String DEV_SUFFIX = "/";
    }

    public interface Key{
        String FACE_H_MIRROR = "faceHorizontalMirror";
        String FACE_V_MIRROR = "faceVerticalMirror";
        String LIVE_ENABLED = "liveEnabled";
        String RGB_CAMERA_ANGLE = "cameraAngle";
        String SIMILAR_THRESHOLD = "similarThreshold";
    }

    public static class Default{
        public static boolean FACE_H_MIRROR = true;
        public static boolean FACE_V_MIRROR = false;
        public static boolean LIVE_ENABLED = true;
        public static int RGB_CAMERA_ANGLE = 0;
        public static int SIMILAR_THRESHOLD = 70;
    }
}


