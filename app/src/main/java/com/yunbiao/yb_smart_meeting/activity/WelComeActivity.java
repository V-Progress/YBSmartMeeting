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
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.business.MeetingLoader;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.activity.fragment.Intro2Fragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.RecordFragment;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.business.Downloader;
import com.yunbiao.yb_smart_meeting.business.LocateManager;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.business.ResourceCleanManager;
import com.yunbiao.yb_smart_meeting.common.UpdateVersionControl;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.faceview.FaceSDK;
import com.yunbiao.yb_smart_meeting.faceview.FaceView;
import com.yunbiao.yb_smart_meeting.business.KDXFSpeechManager;
import com.yunbiao.yb_smart_meeting.utils.RestartAPPTool;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGpioActivity implements FaceView.FaceCallback {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

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
        if (faceView != null) {
            faceView.setCallback(this);
        }
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);

        KDXFSpeechManager.instance().init(this);

        ivMainCode = find(R.id.iv_main_code);
        ivHead = find(R.id.iv_person_head);
        aivLoading = find(R.id.aiv_person_loading);
        tvPersonName = find(R.id.tv_person_name);
        aivLoading = find(R.id.av_loading);

        //记录Fragment
        recordFragment = new RecordFragment();
        addFragment(R.id.fl_record_container, recordFragment);

        //宣传
        Intro2Fragment introFragment = new Intro2Fragment();
        addFragment(R.id.fl_introduce_container, introFragment);

        faceView.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goSetting();
            }
        });

//        String name = SpUtils.getStr(SpUtils.COMPANY_NAME);
//        if (!TextUtils.isEmpty(name)) tvMainAbbName.setText(name);
        String logoUrl = SpUtils.getStr(SpUtils.COMPANY_LOGO);
        Glide.with(this).load(logoUrl).asBitmap().into(ivMainLogo);
    }

    @Override
    protected void initData() {
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

    //准备就绪
    @Override
    public void onReady() {
        //自动清理服务
        ResourceCleanManager.instance().startAutoCleanService();

        //初始化会议信息
        MeetingLoader.i().startAutoGetMeeting(loadListener);
    }

    @Override
    public void onFaceDetection(Boolean hasFace) {
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

        RecordManager.get().checkPassage(verifyResult, verifyCallback);
    }

    private RecordManager.VerifyCallback verifyCallback = new RecordManager.VerifyCallback() {
        @Override
        public void onVerifySuccess(RecordInfo recordInfo) {
            KDXFSpeechManager.instance().playText(recordInfo.getName() + "，欢迎参加本次会议");
        }
    };

    private MeetingLoader.LoadListener loadListener = new MeetingLoader.LoadListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onError() {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_MEETING_FAILED));
        }

        @Override
        public void onSuccess() {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_COMPLETE_SUCESS));
            //加载当前会议
            MeetingLoader.i().loadCurrentMeeting(loadListener);

            //初始化签到记录
            RecordManager.get();
        }

        @Override
        public void noMeeting() {
            loadQRCode(null);
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_NO_MEETING));
            RecordManager.get().clearAllRecord();
        }

        @Override
        public void onFinish() {
        }

        @Override
        public void onPreload(final MeetInfo currentMeetInfo) {
            tvMainAbbName.post(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(currentMeetInfo.getMeetRoomName()))
                        tvMainAbbName.setText(currentMeetInfo.getMeetRoomName());
                }
            });

            loadQRCode(currentMeetInfo);
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_PRELOAD, currentMeetInfo));
            loadUser(currentMeetInfo);
        }

        @Override
        public void onBegan(MeetInfo currentMeetInfo) {
            loadQRCode(currentMeetInfo);
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_BEGAN, currentMeetInfo));
            loadUser(currentMeetInfo);
        }

        @Override
        public void onEnded(MeetInfo currentMeetInfo) {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_ENDED, currentMeetInfo));

            DaoManager.get().deleteAllByMeetId(currentMeetInfo.getId());
        }

        @Override
        public void onNextMeet(MeetInfo nextMeetInfo) {

        }
    };

    private void loadQRCode(final MeetInfo meetInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(meetInfo == null){
                    Glide.clear(ivMainCode);
                    return;
                }
                Log.e(TAG, "run: 加载二维码");
                String codeUrl = meetInfo.getCodeUrl();
                Glide.with(WelComeActivity.this).load(codeUrl).asBitmap().into(ivMainCode);
            }
        });
    }

    private void clearUser() {
        FaceSDK.instance().removeAllUser(new FaceUserManager.FaceUserCallback() {
            @Override
            public void onUserResult(boolean b, int i) {
                Log.e(TAG, "onUserResult: " + b + " --- " + i);
                aivLoading.setVisibility(View.GONE);
            }
        });
    }

    private void loadUser(final MeetInfo meetInfo) {
        clearUser();
        RecordManager.get().setCurrMeet(meetInfo.getId());
        Downloader.downloadHead(meetInfo, new Downloader.DownloadListener() {
            @Override
            public void complete(final List<EntryInfo> entryInfos) {
                Queue<EntryInfo> entryQueue = new LinkedList<>();
                entryQueue.addAll(entryInfos);

                Map<String, FaceUser> allFaceData = FaceSDK.instance().getAllFaceData();
                addUser(entryQueue, allFaceData, new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    private void addUser(final Queue<EntryInfo> entryQueue, final Map<String, FaceUser> allFaceData, final Runnable runnable) {
        d("准备添加");
        if (entryQueue == null || entryQueue.size() <= 0) {
            d("添加完毕");
            runnable.run();
            return;
        }

        EntryInfo poll = entryQueue.poll();
        if (!new File(poll.getHeadPath()).exists()) {
            Log.e(TAG, "onUserResult: 头像不存在");
            addUser(entryQueue, allFaceData, runnable);
            return;
        }

        if (allFaceData.containsKey(poll.getId())) {
            d("更新人脸");
            FaceUser faceUser = allFaceData.get(poll.getId());
            faceUser.setImagePath(poll.getHeadPath());
            FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean b, int i) {
                    Log.e(TAG, "onUserResult: 更新结果：" + b + " --- " + i);
                    addUser(entryQueue, allFaceData, runnable);
                }
            });
        } else {
            d("添加人脸");
            FaceSDK.instance().addUser(poll.getId() + "", poll.getHeadPath(), new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean b, int i) {
                    Log.e(TAG, "onUserResult: 添加结果：" + b + " --- " + i);
                    addUser(entryQueue, allFaceData, runnable);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo event) {
        Log.e(TAG, "update: 收到签到更新事件" + event.toString());

        recordFragment.addRecord(event);
        Glide.with(this).load(event.getHeadPath()).asBitmap().into(ivHead);
        tvPersonName.setText(event.getName());

        ivHead.removeCallbacks(resetRunnable);
        ivHead.postDelayed(resetRunnable, 3 * 1000);
    }

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            Glide.with(WelComeActivity.this).load(R.mipmap.img_noface).asBitmap().into(ivHead);
            tvPersonName.setText("");
        }
    };

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
        APP.stopXMPP();
        LocateManager.instance().destory();
    }

}