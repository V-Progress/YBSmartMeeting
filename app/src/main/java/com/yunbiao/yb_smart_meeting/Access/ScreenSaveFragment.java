package com.yunbiao.yb_smart_meeting.Access;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ScreenSaveFragment extends BaseFragment {
    private static final String TAG = "ScreenSaveFragment";
    private FragmentManager fragmentManager;
    private View rootView;
    private View flClock;
    private View llMeetingCurr;
    private TextView tvName;
    private TextView tvTime;
    private TextView tvState;

    public static ScreenSaveFragment newInstance() {
        return new ScreenSaveFragment();
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_screen_save;
    }

    @Override
    protected void initView() {
        fragmentManager = getFragmentManager();
        rootView = getView();
        flClock = rootView.findViewById(R.id.fl_clock);
        llMeetingCurr = rootView.findViewById(R.id.ll_meeting_curr);
        tvName = rootView.findViewById(R.id.tv_curr_meeting_name);
        tvTime = rootView.findViewById(R.id.tv_curr_meeting_time);
        tvState = rootView.findViewById(R.id.tv_curr_meeting_state);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
        hideSelf();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event) {
        int state = event.getState();
        MeetInfo meetInfo = event.getMeetInfo();
        if (state == MeetingEvent.NO_MEETING) {
            flClock.setVisibility(View.VISIBLE);
            llMeetingCurr.setVisibility(View.GONE);
        } else if (state == MeetingEvent.PRELOAD || state == MeetingEvent.BEGAN/* || state == MeetingEvent.ENDED*/) {
            flClock.setVisibility(View.GONE);
            llMeetingCurr.setVisibility(View.VISIBLE);
            tvName.setText(meetInfo.getName());
            tvTime.setText(meetInfo.getBeginTime() + "~" + meetInfo.getEndTime());
            int color;
            String stateStr;
            if (state == MeetingEvent.PRELOAD) {
                color = Color.WHITE;
                stateStr = "即将开始";
            } else {
                color = Color.GREEN;
                stateStr = "正在进行";
            }/* else {
                color = Color.RED;
                stateStr = "已结束";
            }*/
            tvState.setTextColor(color);
            tvState.setText(stateStr);
        }
    }

    public void hasPerson() {
        hideSelf();
    }

    private void showSelf() {
        if (isHidden()) {
            beginTransaction().show(this).commitAllowingStateLoss();
        }
    }

    private void hideSelf() {
        if (!isHidden()) {
            beginTransaction().hide(this).commitAllowingStateLoss();
        }
        rootView.removeCallbacks(runnable);
        rootView.postDelayed(runnable, showTime);
    }

    private FragmentTransaction beginTransaction() {
        return fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.screen_top_in, R.anim.screen_top_out);
    }

    private long showTime = 10000;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showSelf();
        }
    };
}
