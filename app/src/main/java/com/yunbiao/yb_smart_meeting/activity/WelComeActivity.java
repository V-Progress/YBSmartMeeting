package com.yunbiao.yb_smart_meeting.activity;

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
import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.activity.fragment.IntroFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.RecordFragment;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.bean.CompanyBean;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.business.LocateManager;
import com.yunbiao.yb_smart_meeting.business.MeetingManager;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.business.ResourceCleanManager;
import com.yunbiao.yb_smart_meeting.common.UpdateVersionControl;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.faceview.FaceResult;
import com.yunbiao.yb_smart_meeting.faceview.FaceView;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.RestartAPPTool;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.xmpp.ServiceManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGpioActivity {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

    // xmpp推送服务
    private ServiceManager serviceManager;

    //摄像头分辨率
    private FaceView faceView;
    private RecordFragment recordFragment;
    private ImageView ivHead;
    private View aivLoading;
    private TextView tvPersonName;
    private ImageView ivMainCode;

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
        faceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    goSetting();
                }
                return true;
            }
        });
        if (faceView != null) {
            faceView.setCallback(faceCallback);
        }
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);

        ivMainCode = find(R.id.iv_main_code);
        ivHead = find(R.id.iv_person_head);
        aivLoading = find(R.id.aiv_person_loading);
        tvPersonName = find(R.id.tv_person_name);
        aivLoading = find(R.id.av_loading);

        //记录Fragment
        recordFragment = new RecordFragment();
        addFragment(R.id.fl_record_container, recordFragment);

        //宣传
        IntroFragment introFragment = new IntroFragment();
        addFragment(R.id.fl_introduce_container,introFragment);
    }

    @Override
    protected void initData() {
        //开启Xmpp
        startXmpp();

        //初始化定位工具
        LocateManager.instance().init(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: ------- ");
                UpdateVersionControl.getInstance().checkUpdate(WelComeActivity.this);
            }
        }, 5 * 1000);
    }

    boolean isInited = false;

    /*人脸识别回调，由上到下执行*/
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            if(isInited){
                return;
            }
            loadCompany();
            RecordManager.get();
            ResourceCleanManager.instance().startAutoCleanService();
            isInited = true;
        }

        @Override
        public void onFaceDetection(FaceResult result) {
            if (isAlwayOpen()) {
                return;
            }
            onLight();
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if (isAlwayOpen()) {
                return;
            }

            RecordManager.get().checkPassage(verifyResult);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo event) {
        Log.e(TAG, "update: 收到签到更新事件" +event.toString() );

        recordFragment.addRecord(event);
        Glide.with(this).load(event.getHeadPath()).asBitmap().into(ivHead);
        tvPersonName.setText(event.getName());

        ivHead.removeCallbacks(resetRunnable);
        ivHead.postDelayed(resetRunnable,3 * 1000);
    }

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            Glide.with(WelComeActivity.this).load(R.mipmap.img_noface).asBitmap().into(ivHead);
            tvPersonName.setText("");
        }
    };

    private void loadCompany(){
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: ----- " + response);
                if(TextUtils.isEmpty(response)){
                    return;
                }
                CompanyBean companyBean = new Gson().fromJson(response, CompanyBean.class);

                int comid = companyBean.getCompany().getComid();
                String name = companyBean.getCompany().getComname();
                String pwd = companyBean.getCompany().getDevicePwd();
                String logoUrl = companyBean.getCompany().getComlogo();

                //保存系统信息
                saveCompanyInfo(comid,name,pwd,logoUrl);

                //发送更新事件
                EventBus.getDefault().postSticky(new SysInfoUpdateEvent());

                //初始化会议信息
                MeetingManager.getInstance().init();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event) {
        aivLoading.setVisibility(View.VISIBLE);
        Log.e(TAG, "update: 收到会议更新事件" );
        int state = event.getState();
        final MeetInfo meetInfo = event.getMeetInfo();
        switch (state) {
            case MeetingEvent.PRELOADING:
                break;
            case MeetingEvent.BEGINED:
                Glide.with(this).load(meetInfo.getCodeUrl()).asBitmap().into(ivMainCode);
                d("会议已开始，添加用户");
                RecordManager.get().setEntryList(meetInfo);
                break;
            case MeetingEvent.END:

                break;
        }
        aivLoading.setVisibility(View.GONE);
    }

    private void saveCompanyInfo(int id, String name, String pwd,String logoUrl) {
        SpUtils.saveInt(SpUtils.COMPANY_ID, id);
        SpUtils.saveStr(SpUtils.COMPANY_NAME, name);
        SpUtils.saveStr(SpUtils.MENU_PWD, pwd);

        if (!TextUtils.isEmpty(name)) tvMainAbbName.setText(name);
        Glide.with(this).load(logoUrl).asBitmap().into(ivMainLogo);
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
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

    //跳转设置界面
    public void goSetting(View view) {
        goSetting();
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
        destoryXmpp();

        LocateManager.instance().destory();
    }
}