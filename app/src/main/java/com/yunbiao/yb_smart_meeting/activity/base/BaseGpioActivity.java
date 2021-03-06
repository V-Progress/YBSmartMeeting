package com.yunbiao.yb_smart_meeting.activity.base;

import android.app.smdt.SmdtManager;
import android.os.Bundle;
import android.os.Handler;

import com.android.xhapimanager.XHApiManager;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.business.HardwareUtil;
import com.yunbiao.yb_smart_meeting.activity.Event.GpioEvent;
import com.yunbiao.yb_smart_meeting.activity.Event.OpenDoorTimeEvent;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseGpioActivity extends BaseActivity {
    private boolean isAlwayOpen = false;//常开
    private Handler timeHanlder = new Handler();
    private SmdtManager smdt;
    private int CLOSE_DELAY = 8;
    private XHApiManager xhApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        smdt = APP.getSmdt();
        xhApi = APP.getXHApi();
        if(smdt == null && xhApi == null){
//            UIUtils.showShort(this,"无法控制门禁，请检查板卡型号");
        }
    }

    protected boolean isAlwayOpen() {
        return isAlwayOpen;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //缓存获取常开状态并设置开门
        isAlwayOpen = SpUtils.getBoolean(SpUtils.DOOR_STATE,false);
        d("update: ----- " + isAlwayOpen);
        if(isAlwayOpen){
            startAutoCheck(0);
        } else {
            offGate();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(GpioEvent gpioEvent) {
        d("update: ----- 接收到GPIO事件");
        if(gpioEvent.getState() == GpioEvent.OPEN){
            UIUtils.showShort(this,"已解锁");
            openDoor();
            return;
        }

        isAlwayOpen = gpioEvent.isIs();
        d("update: ----- " + isAlwayOpen);
        if(isAlwayOpen){
            UIUtils.showShort(this,"已设置常开");
            openDoor();
        } else {
            UIUtils.showShort(this,"已关闭常开");
            offGate();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(OpenDoorTimeEvent gpioEvent) {
        d("update: ----- 接收到设置门禁时间的事件 " + gpioEvent.getTime());
        if(gpioEvent != null && gpioEvent.getTime() > 0){
            CLOSE_DELAY = gpioEvent.getTime();
        }
    }

    /***
     * 对外的开门方法
     */
    protected void openDoor(){
        onGate();
        if(isAlwayOpen){
            return;
        }
        startAutoGate();
    }

    //开门
    private void onGate(){
        if(smdt != null){
            for (int i = 0; i < 3; i++) {
                int result = smdt.smdtSetExtrnalGpioValue(1, true);
            }
        }

        if(xhApi != null){
            xhApi.XHSetGpioValue(5,0);
        }

        HardwareUtil.writeRelay(APP.getContext(),1);
    }

    //关门
    private void offGate(){
        if(smdt != null){
            for (int i = 0; i < 3; i++) {
                int result = smdt.smdtSetExtrnalGpioValue(1, false);
            }
        }

        if(xhApi != null){
            xhApi.XHSetGpioValue(5,1);
        }

        HardwareUtil.writeRelay(APP.getContext(),0);
    }

    //开灯
    protected void onLight(){
        if(xhApi != null){
            xhApi.XHSetGpioValue(4,1);
        }
        startAutoLight();
    }

    //关灯
    private void offLight(){
        if(xhApi != null){
            xhApi.XHSetGpioValue(4,0);
        }
    }

    /***
     * 开始执行自动门禁
     */
    protected void startAutoGate(){
        timeHanlder.removeCallbacks(gateControlRunnable);
        timeHanlder.postDelayed(gateControlRunnable,CLOSE_DELAY * 1000);
    }

    /***
     * 开始执行自动灯光
     */
    protected void startAutoLight(){
        timeHanlder.removeCallbacks(lightControlRunnable);
        timeHanlder.postDelayed(lightControlRunnable,CLOSE_DELAY * 1000);
    }

    /***
     * 开始执行自动监测
     * @param time
     */
    private void startAutoCheck(long time){
        timeHanlder.removeCallbacks(checkRunnable);
        timeHanlder.postDelayed(checkRunnable,time);
    }

    //常开状态监测
    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            if(isAlwayOpen){
                openDoor();
            }
            startAutoCheck(1000);
        }
    };

    //灯光控制
    private Runnable lightControlRunnable = new Runnable() {
        @Override
        public void run() {
            offLight();
        }
    };

    //门禁控制
    private Runnable gateControlRunnable = new Runnable() {
        @Override
        public void run() {
            if(isAlwayOpen){
                return;
            }
            offGate();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
