package com.yunbiao.yb_smart_meeting.Access;

import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.business.DialogUtil;
import com.yunbiao.yb_smart_meeting.business.Downloader;
import com.yunbiao.yb_smart_meeting.business.HardwareUtil;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.business.ResourceCleanManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.faceview.FaceSDK;
import com.yunbiao.yb_smart_meeting.faceview.FaceView;
import com.yunbiao.yb_smart_meeting.business.KDXFSpeechManager;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class WelComeSmallActivity extends BaseGpioActivity {
    private static final String TAG = "WelComeSmallActivity";

    private FaceView faceView;
    private TextView tvMeeting;
    private ImageView ivLogo;
    private TextView tvTitle;
    private FrameLayout flScreen;
    private ScreenSaveFragment screenSaveFragment;

    @Override
    protected String setTitle() {
        return "首页";
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_wel_come_small;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_wel_come_small;
    }

    @Override
    protected void initView() {
        flScreen = find(R.id.fl_screen);
        tvMeeting = find(R.id.tv_meeting);
        ivLogo = find(R.id.iv_logo);
        tvTitle = find(R.id.tv_title);
        faceView = findViewById(R.id.face_view);
        faceView.setCallback(faceCallback);

        KDXFSpeechManager.instance().init(this);

        String name = SpUtils.getStr(SpUtils.COMPANY_NAME);
        String logoUrl = SpUtils.getStr(SpUtils.COMPANY_LOGO);
        if (!TextUtils.isEmpty(name)) tvTitle.setText(name);
        Glide.with(this).load(logoUrl).asBitmap().into(ivLogo);

        if (screenSaveFragment == null || !screenSaveFragment.isAdded()) {
            screenSaveFragment = ScreenSaveFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fl_screen, screenSaveFragment)./*hide(screenSaveFragment).*/commit();
        }
    }

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {

        @Override
        public void onReady() {
            //自动清理服务
            ResourceCleanManager.instance().startAutoCleanService();

            MeetingLoader.i().startAutoGetMeeting(loadListener);

            HardwareUtil.startLightDetection();
        }

        @Override
        public void onFaceDetection(Boolean hasFace) {
            if (isAlwayOpen()) {
                return;
            }
            if (hasFace) {
                if(screenSaveFragment != null){
                    screenSaveFragment.hasPerson();
                }
                onLight();
            }
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            Log.e(TAG, "onFaceVerify: 认证结果：" + verifyResult.getResult());
            FaceUser user = verifyResult.getUser();
            if (user != null) {
                Log.e(TAG, "onFaceVerify: " + user.getUserId());
            }

            if (isAlwayOpen()) {
                return;
            }

            RecordManager.get().checkPassage(verifyResult,verifyCallback);
        }
    };

    private RecordManager.VerifyCallback verifyCallback = new RecordManager.VerifyCallback() {
        @Override
        public void onVerifySuccess(final RecordInfo recordInfo) {
            Log.e(TAG, "onVerifySuccess: " + recordInfo.toString());
//            HardwareUtil.openDoor();
            openDoor();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    KDXFSpeechManager.instance().playText(recordInfo.getName() + "，欢迎参加本次会议");
                    Toast.makeText(WelComeSmallActivity.this, recordInfo.getName() + "你好\n认证成功，请通过", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private MeetingLoader.LoadListener loadListener = new MeetingLoader.LoadListener() {
        @Override
        public void onStart() {
            Log.e(TAG, "onStart: 1111111111111");
            DialogUtil.showProgress(WelComeSmallActivity.this, "正在加载会议信息...");
        }

        @Override
        public void onError() {
            Log.e(TAG, "onError: 222222222222222222");
            setMeetingName("获取会议失败");
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_MEETING_FAILED));
        }

        @Override
        public void onSuccess() {
            Log.e(TAG, "onSuccess: 33333333333333333" );
            DialogUtil.showProgress(WelComeSmallActivity.this, "正在加载当前会议...");
            setMeetingName("");

            //加载当前会议
            MeetingLoader.i().loadCurrentMeeting(loadListener);

            //初始化签到记录
            RecordManager.get();
        }

        @Override
        public void noMeeting() {
            setMeetingName("暂无会议");
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_NO_MEETING));
            clearUser();
        }

        @Override
        public void onFinish() {
            DialogUtil.dismissProgress(WelComeSmallActivity.this);
        }

        @Override
        public void onPreload(MeetInfo currentMeetInfo) {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_PRELOAD,currentMeetInfo));
            loadUser(currentMeetInfo);
        }

        @Override
        public void onBegan(MeetInfo currentMeetInfo) {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_BEGAN,currentMeetInfo));
            loadUser(currentMeetInfo);
        }

        @Override
        public void onEnded(MeetInfo currentMeetInfo) {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.LOAD_ENDED,currentMeetInfo));
            MeetingLoader.i().getAllMeeting(loadListener);
        }

        @Override
        public void onNextMeet(MeetInfo nextMeetInfo) {
            String s = tvMeeting.getText().toString();
            String next = "下个会议：" +
                    (nextMeetInfo == null ? "无"
                    : nextMeetInfo.getName()
                    + "\n会议主讲：" + nextMeetInfo.getUserName()
                    + "\n开始时间：" + nextMeetInfo.getBeginTime()
                    + "\n结束时间：" + nextMeetInfo.getEndTime());
            setMeetingName(s + "\n\n" + next);
        }
    };

    private void setMeetingName(final String name){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMeeting.setText(name);
            }
        });
    }

    private void loadUser(final MeetInfo meetInfo){
        setMeetingName("当前会议：" + meetInfo.getName()
                + "\n会议主讲：" + meetInfo.getUserName()
                + "\n开始时间：" + meetInfo.getBeginTime()
                + "\n结束时间：" + meetInfo.getEndTime());

        tvMeeting.postDelayed(new Runnable() {
            @Override
            public void run() {
                MeetingLoader.i().loadNext(meetInfo.getNum(),loadListener);
            }
        },2000);

        RecordManager.get().setCurrMeet(meetInfo.getId());

        DialogUtil.showProgress(WelComeSmallActivity.this,"正在下载头像");
        Downloader.downloadHead(meetInfo, new Downloader.DownloadListener() {
            @Override
            public void complete(final List<EntryInfo> entryInfos) {
                Queue<EntryInfo> entryQueue = new LinkedList<>();
                entryQueue.addAll(entryInfos);

                Map<String, FaceUser> allFaceData = FaceSDK.instance().getAllFaceData();
                addUser(entryQueue, allFaceData, new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.dismissProgress(WelComeSmallActivity.this);

                    }
                });
            }
        });
    }

    private void clearUser() {
        FaceSDK.instance().removeAllUser(new FaceUserManager.FaceUserCallback() {
            @Override
            public void onUserResult(boolean b, int i) {
                Log.e(TAG, "onUserResult: " + b + " --- " + i);
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

        String userId = String.valueOf(poll.getId());
        if (allFaceData.containsKey(userId)) {
            d("更新人脸");
            FaceUser faceUser = allFaceData.get(userId);
            if (!TextUtils.equals(faceUser.getImagePath(),poll.getHeadPath())) {
                faceUser.setImagePath(poll.getHeadPath());
                FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                    @Override
                    public void onUserResult(boolean b, int i) {
                        Log.e(TAG, "onUserResult: 更新结果：" + b + " --- " + i);
                        addUser(entryQueue, allFaceData, runnable);
                    }
                });
            } else {
                Log.e(TAG, "onUserResult: 不需要更新");
                addUser(entryQueue, allFaceData, runnable);
            }
        } else {
            d("添加人脸");
            FaceSDK.instance().addUser(userId, poll.getHeadPath(), new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean b, int i) {
                    Log.e(TAG, "onUserResult: 添加结果：" + b + " --- " + i);
                    addUser(entryQueue, allFaceData, runnable);
                }
            });
        }
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
    }

    @Override
    public void onBackPressed() {
        APP.exit();
//        RestartAPPTool.showExitDialog(this, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                inputPwd(new Runnable() {
//                    @Override
//                    public void run() {
//                        moveTaskToBack(true);
//                    }
//                });
//            }
//        }, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                inputPwd(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
//            }
//        });
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

        final Animation animation = AnimationUtils.loadAnimation(WelComeSmallActivity.this, R.anim.anim_edt_shake);
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

}
