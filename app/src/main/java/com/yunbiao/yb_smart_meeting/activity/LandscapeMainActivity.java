package com.yunbiao.yb_smart_meeting.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.MeetingListFragment;
import com.yunbiao.yb_smart_meeting.afinel.PathManager;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.business.DataLoader;
import com.yunbiao.yb_smart_meeting.business.KDXFSpeechManager;
import com.yunbiao.yb_smart_meeting.business.LocateManager;
import com.yunbiao.yb_smart_meeting.business.MeetLoader;
import com.yunbiao.yb_smart_meeting.business.MeetTimer;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.utils.RestartAPPTool;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class LandscapeMainActivity extends BaseGpioActivity {


    private FaceView faceView;
    private RecyclerView rlvMeeting;
    private ImageView ivLogo;
    private TextView tvName;

    @Override
    protected String setTitle() {
        return null;
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_landscape_main;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_landscape_main;
    }

    @Override
    protected void initView() {
        APP.setActivity(this);

        rlvMeeting = find(R.id.rlv_meeting);
        faceView = find(R.id.face_view);
        tvName = find(R.id.tv_name);
        ivLogo = find(R.id.iv_logo);
        faceView.setCallback(faceCallback);

        rlvMeeting.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rlvMeeting.addItemDecoration(new ItemRightDecoration(3));

        String logoUrl = SpUtils.getStr(SpUtils.COMPANY_LOGO);
        Glide.with(this).load(logoUrl).asBitmap().into(ivLogo);

        KDXFSpeechManager.instance().init(this);
    }

    public void exit(View view) {
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


    public void goSetting(View view) {

        goSetting();
    }


    class MeetingAdapter extends RecyclerView.Adapter<VH> {
        private List<MeetInfo> meetInfoList;

        public MeetingAdapter(List<MeetInfo> meetInfoList) {
            this.meetInfoList = meetInfoList;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_meeting_land, null);
            return new VH(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull VH vh, int i) {
            vh.bind(meetInfoList.get(i));
        }

        @Override
        public int getItemCount() {
            return meetInfoList.size();
        }
    }

    public class ItemRightDecoration extends RecyclerView.ItemDecoration {
        private int margin = 0;

        public ItemRightDecoration(int value) {
            margin = value;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right += margin;
        }
    }

    class VH extends RecyclerView.ViewHolder {
        private final TextView tvTime;
        private final TextView tvName;
        private final TextView tvUser;
        private final TextView tvIndi;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_meet_name);
            tvUser = itemView.findViewById(R.id.tv_meet_user);
            tvTime = itemView.findViewById(R.id.tv_meet_time);
            tvIndi = itemView.findViewById(R.id.tv_meet_indicator);
        }

        public void bind(MeetInfo meetInfo) {
            tvName.setText("会议名称：" + meetInfo.getName());
            tvUser.setText("会议主讲：" + meetInfo.getUserName());
            tvTime.setText(meetInfo.getBeginTime() + "\n~\n" + meetInfo.getEndTime());

            int num = meetInfo.getNum();
            if (num == 1) {
                tvIndi.setTextColor(Color.GREEN);
                tvIndi.setText("当前会议");
            } else if (num == 2) {
                tvIndi.setTextColor(LandscapeMainActivity.this.getResources().getColor(R.color.font_green));
                tvIndi.setText("下个会议");
            } else {
                tvIndi.setVisibility(View.INVISIBLE);
            }
        }
    }

    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            FaceManager.getInstance().init(APP.getContext(), PathManager.FEATURE_PATH);

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


    private DataLoader.LoadMeetCallback loadMeetCallback = new DataLoader.LoadMeetCallback() {
        @Override
        public void onStartGetMeeting() {
            dismissBindDialog();
        }

        @Override
        public void notBind() {
            showBindDialog();
        }

        @Override
        public void onErrorGetMeeting(Exception e) {
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_MEETING_FAILED));
        }

        @Override
        public void onErrorUpdateMeeting(Exception e) {

        }

        @Override
        public void noMeeting() {
            DataLoader.i().clearDataByCurrCompany();
            FaceManager.getInstance().clearCache();//没有会议的时候清除特征库缓存
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_NO_MEETING));

            setRlvMeeting();
        }

        @Override
        public void notChange() {
        }

        @Override
        public void onMeetingGeted(MeetingResponse meetingResponse) {
            //处理数据
            DataLoader.i().handleData(meetingResponse, handleDataCallback);
        }

        @Override
        public void onFinish() {

        }
    };

    private void setRlvMeeting() {
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        List<MeetInfo> meetInfoList = DaoManager.get().queryMeetInfoByComId(comId);
        if (meetInfoList == null) {
            meetInfoList = new ArrayList<>();
        }
        Collections.sort(meetInfoList, new Comparator<MeetInfo>() {
            @Override
            public int compare(MeetInfo o1, MeetInfo o2) {
                return o1.getNum() > o2.getNum() ? 1 : -1;
            }
        });
        rlvMeeting.setAdapter(new MeetingAdapter(meetInfoList));
    }

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

                    setRlvMeeting();
                }
            }, 1 * 1000);
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
//                    tvMainAbbName.setText(meetInfo.getMeetRoomName());//设置会议室名称
//                    loadQRCode(meetInfo);//加载二维码
                    tvName.setText(meetInfo.getMeetRoomName());
                    EventBus.getDefault().post(new MeetingEvent(MeetingEvent.GET_COMPLETE_SUCESS, meetInfo));//发送会议加载事件
                }
            });

            //加载会议时间
            MeetTimer.getInstance().checkTime(meetInfo, meetTimeCallback);
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
            if (state == MeetTimer.STATE_BEFORE || state == MeetTimer.STATE_READY || state == MeetTimer.STATE_ING) {
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
            Log.e(TAG, "onError: ");
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo event) {
        Log.e(TAG, "update: 收到签到更新事件" + event.toString());
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
            faceView.resume();
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

        final Animation animation = AnimationUtils.loadAnimation(LandscapeMainActivity.this, R.anim.anim_edt_shake);
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
                startActivity(new Intent(LandscapeMainActivity.this, SystemActivity.class));
            }
        });
    }

    private static final String TAG = "LandscapeMainActivity";

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

}
