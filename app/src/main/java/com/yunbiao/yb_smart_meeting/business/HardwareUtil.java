package com.yunbiao.yb_smart_meeting.business;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.MacAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bestek.HardwareManager;
import com.bestek.PeopleSensorListener;
import com.yunbiao.yb_smart_meeting.APP;

import java.util.Timer;
import java.util.TimerTask;

public class HardwareUtil {

    public static void setBrightness(Context applicationContext, int brightness) {
        final String service = "HardwareService";
        @SuppressLint("WrongConstant") final HardwareManager manager = (HardwareManager) applicationContext.getSystemService(service);
        if (manager == null) {return;}
        manager.writetoFillinLight(brightness);
    }

    public static int readLight(Context applicationContext){
        // get darkness
        final String service = "HardwareService";
        @SuppressLint("WrongConstant") final HardwareManager manager = (HardwareManager) applicationContext.getSystemService(service);
        if (manager == null) {
            return 0;
        }
        final int darkness = manager.readfromLightSensor();
        return darkness;
    }

    public static void read(Context applicationContext){
        // get darkness
        final String service = "HardwareService";
        @SuppressLint("WrongConstant") final HardwareManager manager = (HardwareManager) applicationContext.getSystemService(service);
    }

    private static final String TAG = "HardwareUtil";
    private static int DARKNESS_1 = 650;
    private static int DARKNESS_2 = 700;
    private static int DARKNESS_3 = 750;
    private static int DARKNESS_4 = 800;
    private static int DARKNESS_5 = 850;
    static Timer timer ;
    public static void startLightDetection(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int i = readLight(APP.getContext());
                Log.e(TAG, "run: 黑度：" + i);

                int bright
                        = i >= DARKNESS_5 ? 5
                        : i >= DARKNESS_4 ? 4
                        : i >= DARKNESS_3 ? 3
                        : i >= DARKNESS_2 ? 2
                        : i >= DARKNESS_1 ? 1
                        : 0 ;
                setBrightness(APP.getContext(),bright);
            }
        },3000,5000);
    }

    public static void setPeopleListener(Context applicationContext,PeopleSensorListener peopleSensorListener){
        // get darkness
        final String service = "HardwareService";
        @SuppressLint("WrongConstant") final HardwareManager manager = (HardwareManager) applicationContext.getSystemService(service);
        if (manager == null) {
            return ;
        }
        manager.SetPeopleSensorListener(peopleSensorListener);
    }

    public static void writeRelay(Context applicationContext,int relay){
        // get darkness
        final String service = "HardwareService";
        @SuppressLint("WrongConstant") final HardwareManager manager = (HardwareManager) applicationContext.getSystemService(service);
        if (manager == null) {
            return ;
        }

        manager.writetoRelay(relay);
    }

    private static int VALUE_OPEN = 1;
    private static int VALUE_CLOSE = 0;
    private static long closeTime = 5000;
    public static void openDoor(){
        writeRelay(APP.getContext(),VALUE_OPEN);
        relayHandler.removeMessages(0);
        relayHandler.sendEmptyMessageDelayed(0,closeTime);
    }

    private static Handler relayHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            writeRelay(APP.getContext(),VALUE_CLOSE);
        }
    };
}
