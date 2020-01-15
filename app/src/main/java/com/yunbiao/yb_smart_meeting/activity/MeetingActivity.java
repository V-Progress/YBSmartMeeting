package com.yunbiao.yb_smart_meeting.activity;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceview.CompareResult;
import com.faceview.FaceManager;
import com.faceview.FacePreviewInfo;
import com.faceview.FaceView;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_meeting.afinel.PathManager;
import com.yunbiao.yb_smart_meeting.model.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.business.DataLoader;
import com.yunbiao.yb_smart_meeting.business.KDXFSpeechManager;
import com.yunbiao.yb_smart_meeting.business.LocateManager;
import com.yunbiao.yb_smart_meeting.business.MeetLoader;
import com.yunbiao.yb_smart_meeting.business.MeetTimer;
import com.yunbiao.yb_smart_meeting.business.RecordManager;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.utils.VipDialogManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MeetingActivity extends BaseGpioActivity {

    private static final String TAG = "MeetingActivity";
    private TextView tv_roomName;
    private TextView tv_meetTime_will;
    private TextView tv_meetPlanner_will;
    private TextView tv_meetTheme_will;
    private TextView tv_meetTime_meeting;
    private TextView tv_theme_meeting;
    private TextView tv_meetPlanner_meeting;
    private ImageView iv_qrCode;
    private RecyclerView rl_meetings;
    private RelativeLayout rl_main_will;
    private RelativeLayout rl_main_meeting;
    private RelativeLayout rl_main_noMeet;
    private RelativeLayout rl_meetList;
    private FaceView faceView;
    private AlertDialog bindAlertDialog;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_meeting;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_landscape_meeting;
    }

    @Override
    protected void initView() {
        APP.setActivity(this);

        tv_roomName = findViewById(R.id.tv_roomName);
        tv_meetTime_will = findViewById(R.id.tv_meetTime_will);
        tv_meetPlanner_will = findViewById(R.id.tv_meetPlanner_will);
        tv_meetTheme_will = findViewById(R.id.tv_meetTheme_will);
        tv_meetTime_meeting = findViewById(R.id.tv_meetTime_meeting);
        tv_theme_meeting = findViewById(R.id.tv_theme_meeting);
        tv_meetPlanner_meeting = findViewById(R.id.tv_meetPlanner_meeting);
        iv_qrCode = findViewById(R.id.iv_qrCode);
        rl_meetings = findViewById(R.id.rl_meetings);
        rl_main_will = findViewById(R.id.rl_main_will);
        rl_main_meeting = findViewById(R.id.rl_main_meeting);
        rl_main_noMeet = findViewById(R.id.rl_main_noMeet);
        rl_meetList = findViewById(R.id.rl_meetList);
        faceView = find(R.id.face_view);

        rl_meetings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        faceView.setCallback(faceCallback);
        KDXFSpeechManager.instance().init(this);
    }

    @Override
    protected void initData() {

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

            if (hasFace) {
                showCamera();
            }
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

            VipDialogManager.showVipDialog(MeetingActivity.this, recordInfo);
            KDXFSpeechManager.instance().playText(recordInfo.getName());
            EventBus.getDefault().post(recordInfo);
        }
    };

    private void showCamera() {
        if (!faceView.isShown()) {
            faceView.setVisibility(View.VISIBLE);
        }

        faceView.removeCallbacks(invisibleRunnable);
        faceView.postDelayed(invisibleRunnable, 3 * 1000);
    }

    private Runnable invisibleRunnable = new Runnable() {
        @Override
        public void run() {
            if (faceView.isShown()) {
                faceView.setVisibility(View.INVISIBLE);
            }
        }
    };

    /***
     * 1.请求会议的回调
     */
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
            Log.e(TAG, "noMeeting------- ");

            //没有会议
            switchMeetingMode(rl_main_noMeet);//切换成 没有会议

            rl_meetList.setVisibility(View.GONE);//隐藏会议列表布局
        }

        @Override
        public void notChange() {
            Log.e(TAG, "notChange------- ");

        }

        @Override
        public void onMeetingGeted(MeetingResponse meetingResponse) {
            Log.e(TAG, "onMeetingGeted------- ");
            //处理数据
            DataLoader.i().handleData(meetingResponse, handleDataCallback);
            rl_meetList.setVisibility(View.VISIBLE);//隐藏会议列表布局
        }

        @Override
        public void onFinish() {
            Log.e(TAG, "onFinish------- ");
        }
    };

    /***
     * 2.数据处理完毕的回调
     */
    private DataLoader.HandleDataCallback handleDataCallback = new DataLoader.HandleDataCallback() {
        @Override
        public void onFinished() {
            setRlvMeeting();
            faceView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //加载会议
                    MeetLoader.getInstance().load(meetCallback);
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
            Log.e(TAG, "meetInfo-------->" + meetInfo.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_roomName.setText(meetInfo.getMeetRoomName());//设置会议室名称
                    x.image().bind(iv_qrCode, meetInfo.getCodeUrl());//加载二维码

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
        public void onState(final MeetInfo meetInfo, final int state) {
            Log.e(TAG, "onState: " + meetInfo.getName() + " --- " + state);

            if (state == MeetTimer.STATE_BEFORE || state == MeetTimer.STATE_READY || state == MeetTimer.STATE_ING) {
                RecordManager.get().setCurrMeet(meetInfo.getId());//设置记录管理器里的会议id
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI(meetInfo, state);
                }
            });
        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "onError: ");
        }
    };

    private void setRlvMeeting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                final MeetingAdapter meetingAdapter = new MeetingAdapter(meetInfoList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rl_meetings.setAdapter(meetingAdapter);
                    }
                });
            }
        }).start();
    }

    private void updateUI(MeetInfo meetInfo, int state) {
        String startHour = "";
        String endHour = "";

        startHour = cutTime(meetInfo.getBeginTime());
        endHour = cutTime(meetInfo.getEndTime());

        if (state == MeetTimer.STATE_BEFORE || state == MeetTimer.STATE_READY) {
            switchMeetingMode(rl_main_will);//切换成 将要开会
            tv_meetTime_will.setText(startHour + "-" + endHour);
            tv_meetPlanner_will.setText(meetInfo.getUserName());
            tv_meetTheme_will.setText(meetInfo.getName());
        } else if (state == MeetTimer.STATE_ING) {
            switchMeetingMode(rl_main_meeting);//切换成 会议中
            tv_meetTime_meeting.setText(startHour + "-" + endHour);
            tv_meetPlanner_meeting.setText(meetInfo.getUserName());
            tv_theme_meeting.setText(meetInfo.getName());

        } else if (state == MeetTimer.STATE_END) {
            switchMeetingMode(rl_main_noMeet);//切换成 没有会议
        }
    }

    private String cutTime(String yearTime) {
        String hourTime = "";
        if (yearTime != null && yearTime.length() > 5) {
            hourTime = yearTime.substring(yearTime.length() - 5, yearTime.length());
        }
        return hourTime;
    }

    //切换会议显示现况
    private void switchMeetingMode(RelativeLayout targetLayout) {
        rl_main_meeting.setVisibility(View.GONE);
        rl_main_will.setVisibility(View.GONE);
        rl_main_noMeet.setVisibility(View.GONE);

        targetLayout.setVisibility(View.VISIBLE);
    }

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

    @Override
    protected String setTitle() {
        return null;
    }


    class MeetingAdapter extends RecyclerView.Adapter<VH> {
        private List<MeetInfo> meetInfoList;

        public MeetingAdapter(List<MeetInfo> meetInfoList) {
            this.meetInfoList = meetInfoList;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_meeting_new_land, null);
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

    class VH extends RecyclerView.ViewHolder {
        private final TextView tv_meet_time;
        private final TextView tv_meet_planner;
        private final TextView tv_meet_theme;

        public VH(@NonNull View itemView) {
            super(itemView);
            tv_meet_planner = itemView.findViewById(R.id.tv_meet_planner);
            tv_meet_theme = itemView.findViewById(R.id.tv_meet_theme);
            tv_meet_time = itemView.findViewById(R.id.tv_meet_time);
        }

        public void bind(MeetInfo meetInfo) {
            tv_meet_theme.setText(meetInfo.getName());
            tv_meet_planner.setText(meetInfo.getUserName());
            tv_meet_time.setText(cutTime(meetInfo.getBeginTime()) + " - " + cutTime(meetInfo.getEndTime()));

            int num = meetInfo.getNum();
//            if (num == 1) {
//                tvIndi.setTextColor(Color.GREEN);
//                tvIndi.setText("当前会议");
//            } else if (num == 2) {
//                tvIndi.setTextColor(LandscapeMainActivity.this.getResources().getColor(R.color.font_green));
//                tvIndi.setText("下个会议");
//            } else {
//                tvIndi.setVisibility(View.INVISIBLE);
//            }
        }
    }
}
