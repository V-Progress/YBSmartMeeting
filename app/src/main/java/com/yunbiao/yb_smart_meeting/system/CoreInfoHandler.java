package com.yunbiao.yb_smart_meeting.system;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;


import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.activity.Event.GpioEvent;
import com.yunbiao.yb_smart_meeting.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_meeting.activity.WelComeActivity;
import com.yunbiao.yb_smart_meeting.bean.XmppLoginMessage;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.common.MachineDetial;
import com.yunbiao.yb_smart_meeting.common.SoundControl;
import com.yunbiao.yb_smart_meeting.common.UpdateVersionControl;
import com.yunbiao.yb_smart_meeting.common.power.PowerOffTool;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.utils.*;
import com.yunbiao.yb_smart_meeting.utils.logutils.LogUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import okhttp3.Call;

/**
 * xmpp消息处理
 *
 * @author Administrator
 */
public class CoreInfoHandler {
    private static final String TAG = "CoreInfoHandler";

    private static final int ONLINE_TYPE = 1;// 上线
    private static final int VOICE_TYPE = 3;// 声音
    private static final int CUTSCREN_TYPE = 4;// 截屏
    private static final int RUNSET_TYPE = 5;// 设备开关机设置
    private static final int SHOW_SERNUM = 6;// 显示设备编号
    private static final int SHOW_VERSION = 7;// 显示版本号
    private static final int SHOW_DISK_IFNO = 8;// 获取磁盘容量
    private static final int POWER_RELOAD = 9;// 设备 开机 重启
    private static final int PUSH_TO_UPDATE = 10;//软件升级
    private final static int ADS_PUSH = 23;//广告更新推送
    private final static int UPDATE_STAFF = 26;//员工信息更新
    private final static int OPEN_DOOR = 99; //开门
    private final static int ALWAYS_OPEN = 100;//门禁常开
    private final static int UPDATE_COMPANY = 101;//更新公司信息
    private final static int UPDATE_INTRODUCE = 33;
    private final static int UPDATE_MEETING = 31;//更新会议信息

    public static boolean isOnline = false;

    public static void messageReceived(String message) {
        LogUtils.e(TAG, "接收消息：" + message);

        XmppLoginMessage xmppLoginMessage = new Gson().fromJson(message, XmppLoginMessage.class);
        XmppLoginMessage.Content content = xmppLoginMessage.getContent();
        int type = xmppLoginMessage.getType();
        switch (type) {
            case ONLINE_TYPE:
                isOnline = true;

                //设备绑定码
                String bindCode = content.getPwd();
                SpUtils.saveStr(SpUtils.BINDCODE, bindCode);

                //设备编号
                String serNum = content.getSerNum();
                SpUtils.saveStr(SpUtils.DEVICE_NUMBER, serNum);

                //设备过期时间
                String expireDate = content.getExpireDate();
                SpUtils.saveStr(SpUtils.EXP_DATE, expireDate);

                //无用
                String runKey = content.getRunKey();
                SpUtils.saveStr(SpUtils.RUN_KEY, runKey);

                //设备类型
                int dtype = content.getDtype();
                SpUtils.saveInt(SpUtils.DEVICE_TYPE, dtype);

                //更新设备类型
                updateDeviceType();

                //发送硬件信息
                MachineDetial.getInstance().upLoadHardWareMessage();

                Log.e(TAG, "messageReceived: ---- 发送系统信息更新事件");
                EventBus.getDefault().postSticky(new SysInfoUpdateEvent());

                //初始化同步
                int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
                if (comId == 0) {

                }
                break;
            case VOICE_TYPE:// 声音控制
                SoundControl.setMusicSound(content.getVoice());
                break;
            case CUTSCREN_TYPE:
                final ScreenShotUtil instance = ScreenShotUtil.getInstance();
                instance.takeScreenshot(APP.getContext(), new ScreenShotUtil.ScreenShotCallback() {
                    @Override
                    public void onShotted(boolean isSucc, String filePath) {
                        String sid = HeartBeatClient.getDeviceNo();
                        instance.sendCutFinish(sid, filePath);
                    }
                });
                break;
            case RUNSET_TYPE:
                ThreadUitls.runInThread(new Runnable() {
                    @Override
                    public void run() {// 开关机时间设置
                        PowerOffTool.getPowerOffTool().getPowerOffTime(HeartBeatClient.getDeviceNo());
                    }
                });
                break;
            case SHOW_SERNUM:
                int showType = content.getShowType();
                if (showType == 0) {//状态栏  视美泰主板
                    int showValue = content.getShowValue();
                    if (showValue == 0) {//显示
                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
                    } else if (showValue == 1) {//隐藏
                        APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
                    }
                } else { // 显示设备编号
                    UIUtils.showTitleTip(APP.getContext(), SpUtils.getStr(SpUtils.DEVICE_NUMBER));
                }
                break;
            case SHOW_VERSION:// 版本信息
                ResourceUpdate.uploadAppVersion();
                break;
            case SHOW_DISK_IFNO:
                int flag = content.getFlag();
                if (flag == 0) { //显示
                    ResourceUpdate.uploadDiskInfo();
                } else if (flag == 1) {// 清理磁盘
                    ResourceUpdate.uploadDiskInfo();
                }
                break;
            case POWER_RELOAD:// 机器重启
                int restart = content.getRestart();
                if (restart == 0) {
                    ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(APP.getContext());
                    progressDialog.setTitle("关机");
                    progressDialog.setMessage("3秒后将关闭设备");
                    progressDialog.show();
                    UIUtils.powerShutDown.start();
                } else if (restart == 1) {
                    ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(APP.getContext());
                    progressDialog.setTitle("重启");
                    progressDialog.setMessage("3秒后将重启设备");
                    progressDialog.show();
                    UIUtils.restart.start();
                }
                break;
            case PUSH_TO_UPDATE:
                Activity activity = APP.getActivity();
                UpdateVersionControl.getInstance().checkUpdate(activity);
                break;
            case ADS_PUSH:
                break;
            case UPDATE_STAFF:
                break;
            case OPEN_DOOR:
                EventBus.getDefault().postSticky(new GpioEvent(GpioEvent.OPEN));
                break;
            case ALWAYS_OPEN:
                boolean doorState = SpUtils.getBoolean(SpUtils.DOOR_STATE, false);
                boolean newState = !doorState;
                EventBus.getDefault().postSticky(new GpioEvent(newState));
                SpUtils.saveBoolean(SpUtils.DOOR_STATE, newState);
                break;
            case UPDATE_COMPANY:
                break;
            case UPDATE_INTRODUCE:
                break;
            case UPDATE_MEETING:
                break;
            default:
                break;
        }
    }

    public static void updateDeviceType() {
        OkHttpUtils.post()
                .url(ResourceUpdate.UPDATE_DEVICE_TYPE)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .addParams("type", "5")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onErrorGetMeeting: " + (e == null ? "NULL" : e.getMessage()));
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "onResponse: " + response);
                    }
                });
    }
}