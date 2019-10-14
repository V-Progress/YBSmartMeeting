package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.child.EntryGirdFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.child.FlowFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.util.List;

public class CurrFragment extends BaseFragment {
    private static final String TAG = "CurrFragment";
    private TextView tvCurrName;
    private TextView tvCurrTime;
    private TextView tvCurrState;

    private EntryGirdFragment insideFragment;
    private EntryGirdFragment outsideFragment;
    private EntryGirdFragment guestFragment;
    private FlowFragment flowFragment;

    @Override
    protected int setLayout() {
        return R.layout.fragment_curr;
    }

    @Override
    protected void initView() {
        tvCurrName = find(R.id.tv_curr_meeting_name);
        tvCurrTime = find(R.id.tv_curr_meeting_time);
        tvCurrState = find(R.id.tv_curr_meeting_state);
    }

    @Override
    protected void initData() {
        showLoading();
    }

    @Override
    public void update(MeetingEvent event) {
        MeetInfo meetInfo = event.getMeetInfo();
        String name = "暂无会议";
        String time = "";
        String state = "";
        int stateColor = 0;
        switch (event.getState()) {
            case MeetingEvent.PRELOAD:
                state = "即将开始";
                name = meetInfo.getName();
                time = meetInfo.getBeginTime() + "   ~   " + meetInfo.getEndTime();
                stateColor = Color.RED;
                loadMeetingInfo(meetInfo);
                break;
            case MeetingEvent.BEGAN:
                state = "正在进行";
                name = meetInfo.getName();
                time = meetInfo.getBeginTime() + "   ~   " + meetInfo.getEndTime();
                stateColor = Color.GREEN;
                loadMeetingInfo(meetInfo);
                break;
            case MeetingEvent.ENDED:
                state = "已结束";
                name = meetInfo.getName();
                time = meetInfo.getBeginTime() + "   ~   " + meetInfo.getEndTime();
                stateColor = Color.GRAY;
                break;
            case MeetingEvent.NO_MEETING:
                showTips("暂无会议");
                break;
        }

        if (stateColor != 0) {
            tvCurrState.setTextColor(stateColor);
        }
        tvCurrState.setText(state);
        tvCurrName.setText(name);
        tvCurrTime.setText(time);
    }

    private void removeFragment(Fragment fragment){
        if(fragment != null && fragment.isAdded()){
            getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    private void addFragment(long meetId,int type,String title){
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryByMeetIdOrType(meetId,type);
        if(entryInfos != null && entryInfos.size() > 0){
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            if(type == 1){
                insideFragment = EntryGirdFragment.newInstance(meetId, title);
                fragmentTransaction.add(R.id.fl_inside_container,insideFragment);
            } else if(type == 2){
                outsideFragment = EntryGirdFragment.newInstance(meetId, title);
                fragmentTransaction.add( R.id.fl_outside_container,outsideFragment);
            } else {
                guestFragment = EntryGirdFragment.newInstance(meetId, title);
                fragmentTransaction.add(R.id.fl_guest_container,guestFragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void loadMeetingInfo(MeetInfo meetInfo) {
        if (meetInfo == null) {
            showTips("获取会议失败");
            return;
        }

        d("加载参会人员");

        removeFragment(insideFragment);
        removeFragment(outsideFragment);
        removeFragment(guestFragment);
        removeFragment(flowFragment);

        long id = meetInfo.getId();
        addFragment(id,3,"参会嘉宾");
        addFragment(id,1,"参会员工");
        addFragment(id,2,"参会访客");

        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(id);
        if(flowInfos != null && flowInfos.size() > 0){
            flowFragment = FlowFragment.newInstance(id);
            getChildFragmentManager().beginTransaction().add(R.id.fl_flow_container,flowFragment).commitAllowingStateLoss();
        }

        hideLoadingAndTips();
    }
}
