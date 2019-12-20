package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.views.NoScrollGridView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class CurrFragment extends BaseFragment {
    private static final String TAG = "CurrFragment";
    private TextView tvCurrName;
    private TextView tvCurrTime;
    private TextView tvCurrState;

    private View llMeetingInfo;
    private EntryAdapter entryAdapter;
    private List<SignModel> signModels = new ArrayList<>();
    private List<FlowInfo> flowInfos = new ArrayList<>();
    private NoScrollGridView entryGridView;
    private NoScrollGridView flowGridView;
    private FlowAdapter flowAdapter;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static CurrFragment instance(String key, long meetId) {
        CurrFragment currFragment = new CurrFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(key, meetId);
        currFragment.setArguments(bundle);
        return currFragment;
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_curr;
    }

    @Override
    protected void initView() {
        tvCurrName = find(R.id.tv_curr_meeting_name);
        tvCurrTime = find(R.id.tv_curr_meeting_time);
        tvCurrState = find(R.id.tv_curr_meeting_state);
        llMeetingInfo = find(R.id.ll_meeting_info);
        entryGridView = find(R.id.grid_view);
        flowGridView = find(R.id.grid_view_flow);

        entryAdapter = new EntryAdapter(getActivity(), signModels);
        entryGridView.setAdapter(entryAdapter);

        flowAdapter = new FlowAdapter(flowInfos);
        flowGridView.setAdapter(flowAdapter);
    }

    @Override
    protected void initData() {
//        long meetId = getArguments().getLong("meetId");
        MeetInfo meetInfo = DaoManager.get().queryMeetInfoByNum(1);
        long meetId = meetInfo.getId();
        tvCurrName.setText(meetInfo.getName());
        tvCurrTime.setText(meetInfo.getBeginTime() + " ~ " + meetInfo.getEndTime());
        tvCurrState.setText(meetInfo.getUserName());

        setEntryInfo(meetId);
        setFlowInfo(meetId);
    }

    private void setFlowInfo(final long meetId) {
        List<FlowInfo> flowInfoList = DaoManager.get().queryFlowByMeetId(meetId);
        Collections.sort(flowInfoList, new Comparator<FlowInfo>() {
            @Override
            public int compare(FlowInfo o1, FlowInfo o2) {
                try {
                    Date o1Begin = dateFormat.parse(o1.getBegin());
                    Date o2Begin = dateFormat.parse(o2.getBegin());
                    return o1Begin.before(o2Begin) ? -1 : 1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        flowInfos.clear();
        flowInfos.addAll(flowInfoList);
        flowAdapter.notifyDataSetChanged();
    }

    private void setEntryInfo(final long meetId) {
        signModels.clear();
        entryAdapter.notifyDataSetChanged();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetId);
                for (EntryInfo entryInfo : entryInfos) {
                    SignModel signModel = new SignModel();
                    boolean signed = DaoManager.get().isSigned(entryInfo.getMeetId(), entryInfo.getMeetEntryId());

                    Log.e(TAG, "initData: ----- " + entryInfo.getMeetEntryId() + " --- " + entryInfo.getName() + " ---签到：" + signed);

                    signModel.setEntryInfo(entryInfo);
                    signModel.setSigned(signed);
                    signModels.add(signModel);
                }

                entryGridView.post(new Runnable() {
                    @Override
                    public void run() {
                        entryAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(RecordInfo recordInfo) {
        if (recordInfo != null) {
            String meetEntryId = recordInfo.getMeetEntryId();
            boolean isUpdate = false;
            if (signModels != null) {
                for (SignModel signModel : signModels) {
                    EntryInfo entryInfo = signModel.getEntryInfo();
                    if (TextUtils.equals(entryInfo.getMeetEntryId(),meetEntryId)) {
                        boolean signed = DaoManager.get().isSigned(entryInfo.getMeetId(), entryInfo.getMeetEntryId());
                        if(signed && signModel.isSigned()){
                            break;
                        }
                        signModel.setSigned(signed);
                        isUpdate = true;
                    }
                }
            }
            if(isUpdate){
                if (entryAdapter != null) {
                    entryAdapter.notifyDataSetChanged();
                }
            }
        }
    }


}
