package com.yunbiao.yb_smart_meeting.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.yunbiao.yb_smart_meeting.APP;

/**
 * Created by LiuShao on 2016/2/21.
 */
public class SpUtils {
    public static final String DOOR_STATE = "doorState";//门禁常开模式
    public static final String COMPANY_INTRO = "companyIntro";
    public static final String MEETING_CACHE = "meetingCache";
    private static SharedPreferences sp;
    private static final String SP_NAME = "YB_FACE";

    public static final String CAMERA_WIDTH = "cameraWidth";//摄像头宽
    public static final String CAMERA_HEIGHT = "cameraHeight";//摄像头高

    public static final String DEVICE_UNIQUE_NO = "deviceNo";//设备唯一号
    public static final String DEVICE_NUMBER = "devicesernum";//设备编号
    public static final String BINDCODE = "bindCode";//绑定码
    public  static final String CITYNAME= "city";//城市

    public static final String COMPANY_ID = "companyid";//公司ID
    public static final String COMPANY_NAME = "companyName";//公司名称
    public static final String COMPANY_LOGO = "companyLogo";//公司logo

    public static final String AD_HENG = "ad_heng";//横屏广告
    public static final String MENU_PWD = "menu_pwd";//用户访问密码
    public static final String EXP_DATE = "expDate";//过期时间
    public static final String IS_MIRROR = "isMirror";//是否镜像
    public static final String BOARD_INFO = "boardInfo";
    public static final String RUN_KEY = "runKey";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String CURR_VOLUME = "currentVolume";
    public static final String CAMERA_ANGLE = "cameraAngle";//摄像头角度
    public static final String LAST_INIT_TIME = "lastInitTime";//上次更新时间

    static {
        sp = APP.getContext().getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static boolean isMirror(){
        return getBoolean(IS_MIRROR,true);
    }

    public static void setMirror(boolean b){
        saveBoolean(IS_MIRROR,b);
    }

    public static void saveStr(String key, String value){
        if(sp != null){
            sp.edit().putString(key,value).commit();
        }
    }

    public static void saveInt(String key,int value){
        if(sp != null){
            sp.edit().putInt(key,value).commit();
        }
    }

    public static void saveLong(String key,long value){
        if(sp != null){
            sp.edit().putLong(key,value).commit();
        }
    }

    public static long getLong(String key){
        if(sp != null){
            return sp.getLong(key,0);
        }
        return 0;
    }

    public static String getStr(String key){
        if(sp != null){
            return sp.getString(key,"");
        }
        return "";
    }
    public static String getStr(String key,String defaultValue){
        if(sp != null){
            return sp.getString(key,defaultValue);
        }
        return defaultValue;
    }

    // TODO: 2019/6/27 ComById
//    public static int getCompanyId(){
//        if(sp != null){
//            return sp.getInt(COMPANY_ID,56);
//        }
//        return -1;
//    }
//
//    public static void saveCompanyId(int comId){
//        if(sp != null){
//            sp.edit().putInt(COMPANY_ID,comId).commit();
//        }
//    }

    public static int getInt(String key){
        if(sp != null){
            return sp.getInt(key,0);
        }
        return 0;
    }

    public static int getIntOrDef(String key,int def){
        if(sp != null){
            return sp.getInt(key,def);
        }
        return def;
    }

    public static void clear(Context context){
        if(sp != null){
            sp.edit().clear().apply();
        }
    }

    public static void saveBoolean(String key,boolean b){
        if(sp != null){
            sp.edit().putBoolean(key,b).commit();
        }
    }

    public static boolean getBoolean(String key,boolean defValue){
        if(sp != null){
            return sp.getBoolean(key,defValue);
        }
        return defValue;
    }

    public static void saveFloat(String key,float value){
        if(sp != null){
            sp.edit().putFloat(key,value).commit();
        }
    }

    public static float getFloat(String key, float defValue){
        if(sp != null){
            return sp.getFloat(key,defValue);
        }
        return defValue;
    }

//    public static void saveString(Context context, String key, String value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        sp.edit().putString(key, value).apply();
//    }
//
//    public static String getString(Context context, String key, String defValue) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        return sp.getString(key, defValue);
//    }
//
//    public static void saveInt(Context context, String key, int value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        sp.edit().putInt(key, value).apply();
//    }
//
//    public static int getInt(Context context, String key, int value) {
//        if (sp == null)
//            sp = context.getSharedPreferences(SP_NAME, 0);
//        return sp.getInt(key, value);
//    }
}
