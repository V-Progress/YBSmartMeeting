package com.yunbiao.yb_smart_meeting.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.faceview.CompareResult;
import com.faceview.FaceManager;
import com.faceview.FacePreviewInfo;
import com.faceview.FaceView;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.afinel.PathManager;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.business.DataLoader;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.activity.fragment.Intro2Fragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.RecordFragment;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.business.LocateManager;
import com.yunbiao.yb_smart_meeting.business.MeetLoader;
import com.yunbiao.yb_smart_meeting.business.MeetTimer;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.common.UpdateVersionControl;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.business.KDXFSpeechManager;
import com.yunbiao.yb_smart_meeting.utils.RestartAPPTool;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGpioActivity {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

    //摄像头分辨率
    private FaceView faceView;
    private ImageView ivHead;
    private TextView tvPersonName;
    private ImageView ivMainCode;
    private RecordFragment recordFragment;
    private Intro2Fragment introFragment;

    @Override
    protected String setTitle() {
        return null;
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void initView() {
        APP.setActivity(this);
        faceView = findViewById(R.id.face_view);
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);

        ivMainCode = find(R.id.iv_main_code);
        ivHead = find(R.id.iv_person_head);
        tvPersonName = find(R.id.tv_person_name);

        KDXFSpeechManager.instance().init(this);
        if (faceView != null) {
            faceView.setCallback(faceCallback);
        }

        faceView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goSetting();
                    }
                });

        String logoUrl = SpUtils.getStr(SpUtils.COMPANY_LOGO);
        Glide.with(this).load(logoUrl).asBitmap().into(ivMainLogo);

        //记录Fragment
        recordFragment = new RecordFragment();
        addFragment(R.id.fl_record_container, recordFragment);

        //宣传
        introFragment = new Intro2Fragment();
        addFragment(R.id.fl_introduce_container, introFragment);
    }

    @Override
    protected void initData() {
        //初始化定位工具
        LocateManager.instance().init(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateVersionControl.getInstance().checkUpdate(WelComeActivity.this);
            }
        }, 5 * 1000);
    }

    /***
     * ===以下是主要业务逻辑=========================================================================================
     */
    /***
     * 人脸识别回调
     */
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            //初始化FaceManager
            FaceManager.getInstance().init(APP.getContext(), PathManager.FEATURE_PATH);

            //初始化会议信息
            DataLoader.i().startAutoGetMeeting(loadMeetCallback);
        }

        @Override
        public void onFaceDetection(Boolean hasFace, List<FacePreviewInfo> facePreviewInfoList) {
            if (isAlwayOpen()) {
                return;
            }
            onLight();
        }

        @Override
        public void onFaceVerify(CompareResult faceAuth) {
            if (isAlwayOpen()) {
                return;
            }

            RecordInfo recordInfo = RecordManager.get().checkPassage(faceAuth);
            if (recordInfo == null) {
                return;
            }
            KDXFSpeechManager.instance().playText(recordInfo.getName());
            EventBus.getDefault().post(recordInfo);
        }
    };

    /***
     * 1.请求会议的回调
     */
    private DataLoader.LoadMeetCallback loadMeetCallback = new DataLoader.LoadMeetCallback() {
        @Override
        public void onStartGetMeeting() {
            Log.e(TAG, "onStartGetMeeting: ");
            dismissBindDialog();
        }

        @Override
        public void notBind() {
            showBindDialog();
            Log.e(TAG, "notBind: ");
        }

        @Override
        public void onErrorGetMeeting(Exception e) {
            Log.e(TAG, "onErrorGetMeeting: ");
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_MEETING_FAILED));
        }

        @Override
        public void onErrorUpdateMeeting(Exception e) {
            Log.e(TAG, "onErrorUpdateMeeting: ");
        }

        @Override
        public void noMeeting() {
            Log.e(TAG, "noMeeting: ");
            DataLoader.i().clearDataByCurrCompany();
            loadQRCode(null);
            FaceManager.getInstance().clearCache();//没有会议的时候清除特征库缓存
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_NO_MEETING));
        }

        @Override
        public void notChange() {
            Log.e(TAG, "notChange: ");
        }

        @Override
        public void onMeetingGeted(MeetingResponse meetingResponse) {
            Log.e(TAG, "onMeetingGeted: ");

            //处理数据
            DataLoader.i().handleData(meetingResponse, handleDataCallback);
        }

        @Override
        public void onFinish() {
            Log.e(TAG, "onFinish: ");
        }
    };

    /***
     * 2.数据处理完毕的回调
     */
    private DataLoader.HandleDataCallback handleDataCallback = new DataLoader.HandleDataCallback() {
        @Override
        public void onFinished() {

            faceView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //加载会议
                    MeetLoader.getInstance().load(meetCallback);
                }
            },1 * 1000);
        }
    };

    /***
     * 3.会议加载回调
     */
    private MeetLoader.MeetCallback meetCallback = new MeetLoader.MeetCallback() {
        @Override
        public void onLoadCurrentMeet(final MeetInfo meetInfo) {
            if (meetInfo == null) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMainAbbName.setText(meetInfo.getMeetRoomName());//设置会议室名称
                    loadQRCode(meetInfo);//加载二维码
                    EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_COMPLETE_SUCESS, meetInfo));//发送会议加载事件
                }
            });

            //加载会议时间
            MeetTimer.getInstance().checkTime(meetInfo,meetTimeCallback);
        }

        @Override
        public void onLoadNextMeet(MeetInfo meetInfo) {

        }
    };

    /***
     * 4.会议时间加载回调
     */
    private MeetTimer.MeetTimeCallback meetTimeCallback = new MeetTimer.MeetTimeCallback() {
        @Override
        public void onState(MeetInfo meetInfo, int state) {
            Log.e(TAG, "onState: " + meetInfo.getName() + " --- " + state);
            if(state == MeetTimer.STATE_BEFORE || state == MeetTimer.STATE_READY || state == MeetTimer.STATE_ING){
                RecordManager.get().setCurrMeet(meetInfo.getId());//设置记录管理器里的会议id
                FaceManager.getInstance().clearCache();//加载前先清除特征库缓存
                MeetLoader.getInstance().loadFaceData(meetInfo.getId());//加载人脸数据
            }/*else if(state == MeetTimer.STATE_END){//会议结束，删除当前会议，修改下一个会议的num为2，并且加载
                DaoManager.get().delete(meetInfo);

                MeetInfo nextMeet = DaoManager.get().queryMeetInfoByNum(2);
                if(nextMeet == null){//下个会议为null时说明没有会议，直接调用加载回调中的noMeeting方法
                    loadMeetCallback.noMeeting();
                    return;
                }
                nextMeet.setNum(1);
                DaoManager.get().update(nextMeet);
                MeetLoader.getInstance().load(meetCallback);
            }*/
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "onError: " );
        }
    };

    /***
     * ==以下是更新UI的方法================================================================================================================
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo event) {
        Log.e(TAG, "update: 收到签到更新事件" + event.toString());

        recordFragment.addRecord(event);
        Glide.with(this)
                .load(event.getHeadPath())
                .asBitmap()
                .transform(new RoundedCornersTransformation(getActivity(), 10, 5))
                .override(100, 100)
                .into(ivHead);
        tvPersonName.setText(event.getName());

        ivHead.removeCallbacks(resetRunnable);
        ivHead.postDelayed(resetRunnable, 3 * 1000);
    }

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            Glide.clear(ivHead);
            tvPersonName.setText("");
        }
    };

    private void loadQRCode(final MeetInfo meetInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (meetInfo == null) {
                    Glide.with(WelComeActivity.this).load(R.mipmap.frame_qrcode).into(ivMainCode);
                    return;
                }
                Log.e(TAG, "run: 加载二维码：" + meetInfo.getCodeUrl());
                x.image().bind(ivMainCode, meetInfo.getCodeUrl());
            }
        });
    }

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (TextUtils.isEmpty(pwd)) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(WelComeActivity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        Button btnBack = dialog.findViewById(R.id.btn_input_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError("不要忘记输入密码哦");
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError("密码错了，重新输入吧");
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
                    runnable.run();
                }
                dialog.dismiss();
            }
        });

        dialog.show();

        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void goSetting() {
        inputPwd(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: ------ " + event.getAction());
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.e(TAG, "onKeyDown: ------ 111111111 " + event.getAction());
            goSetting();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        APP.exit();
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (faceView != null) {
            faceView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (faceView != null) {
            faceView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceView != null) {
            faceView.destory();
        }
        APP.stopXMPP();
        LocateManager.instance().destory();
    }

    private AlertDialog bindAlertDialog;

    private void showBindDialog() {
        if (bindAlertDialog != null && bindAlertDialog.isShowing()) {
            bindAlertDialog.dismiss();
            bindAlertDialog = null;
        }
        String deviceNumber = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        String bindCode = SpUtils.getStr(SpUtils.BINDCODE);
        bindAlertDialog = new AlertDialog.Builder(this)
                .setTitle("编号：" + deviceNumber + "，绑定码：" + bindCode)
                .setMessage("设备未绑定会议室").setCancelable(false).create();
        bindAlertDialog.show();
    }

    private void dismissBindDialog() {
        if (bindAlertDialog != null && bindAlertDialog.isShowing()) {
            bindAlertDialog.dismiss();
            bindAlertDialog = null;
        }
    }

}