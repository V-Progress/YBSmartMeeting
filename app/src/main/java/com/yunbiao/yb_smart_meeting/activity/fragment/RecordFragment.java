package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordFragment extends BaseFragment {
    private static final String TAG = "RecordFragment";
    private View rootView;
    private RecyclerView recyclerView;
    private TextView tvAlready;
    private TextView tvShould;
    private List<RecordInfo> mRecordList = new ArrayList<>();
    private SignAdapter signAdapter;
    private Map<Long,RecordInfo> recordInfoMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = View.inflate(container.getContext(), R.layout.fragment_record, null);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_record;
    }

    @Override
    protected void initView() {
        recyclerView = find(R.id.rlv_record);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = 25;
            }
        });
        signAdapter = new SignAdapter(getActivity(), this.mRecordList);
        recyclerView.setAdapter(signAdapter);

        tvAlready = find(R.id.tv_already);
        tvShould = find(R.id.tv_should);
    }

    @Override
    protected void initData() {
        showLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event) {
        if(event.getState() == MeetingEvent.GET_MEETING_FAILED){
            return;
        }
        clearData();
        Log.e(TAG, "update: 收到会议更新事件");
        MeetInfo meetInfo = event.getMeetInfo();
        int state = event.getState();
        switch (state) {
//            case MeetingEvent.GET_MEETING_FAILED:
//                showTips("获取会议失败");
//                break;
            case MeetingEvent.GET_NO_MEETING:
                showTips("暂无会议");
                break;
            case MeetingEvent.LOAD_PRELOAD:
            case MeetingEvent.LOAD_BEGAN:
                initData(meetInfo);
                break;
            case MeetingEvent.LOAD_ENDED:
                break;
        }
    }

    private void clearData(){
        recordInfoMap.clear();
        mRecordList.clear();
        signAdapter.notifyDataSetChanged();
        intNumber(new ArrayList<EntryInfo>());
    }

    private void initData(MeetInfo meetInfo) {
        if(meetInfo == null){
            showTips("获取会议失败");
            return;
        }

        long id = meetInfo.getId();

        List<RecordInfo> recordInfos = DaoManager.get().queryRecordByMeetId(id);
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
        Collections.sort(recordInfos, new Comparator<RecordInfo>() {
            @Override
            public int compare(RecordInfo o1, RecordInfo o2) {
                //倒序：左大于右返回正值 //正序：左边大时返回负值
                return o1.getTime() < o2.getTime() ? -1 : o1.getTime() > o2.getTime() ? 1 : 0;
            }
        });

        recordInfoMap.clear();
        for (RecordInfo recordInfo : recordInfos) {
            long meetEntryId = recordInfo.getMeetEntryId();
            if (recordInfoMap.containsKey(meetEntryId)) {
                RecordInfo recordInfo1 = recordInfoMap.get(meetEntryId);
                long time1 = recordInfo1.getTime();
                long time = recordInfo.getTime();
                if(time < time1){
                    recordInfoMap.put(meetEntryId,recordInfo);
                }
            } else {
                recordInfoMap.put(meetEntryId,recordInfo);
            }
        }

        mRecordList.clear();
        if (recordInfos != null) {
            mRecordList.addAll(recordInfoMap.values());
        }
        signAdapter.notifyDataSetChanged();

        hideLoadingAndTips();

        intNumber(entryInfos);
    }

    private void intNumber(List<EntryInfo> entryInfos) {
        tvShould.setText(entryInfos.size() + "");
        addNumber();
    }

    private void addNumber() {
        tvAlready.post(new Runnable() {
            @Override
            public void run() {
                tvAlready.setText(recordInfoMap.size() + "");
            }
        });
    }

    public void addRecord(final RecordInfo recordInfo) {
        boolean isUpdate = false;
        long meetEntryId = recordInfo.getMeetEntryId();
        if(recordInfoMap.containsKey(meetEntryId)){
            RecordInfo recordInfo1 = recordInfoMap.get(meetEntryId);
            long time = recordInfo.getTime();
            long time1 = recordInfo1.getTime();
            if(time < time1){
                recordInfoMap.put(meetEntryId,recordInfo);
                isUpdate = true;
            }
        } else {
            recordInfoMap.put(meetEntryId,recordInfo);
            isUpdate = true;
        }

        if(isUpdate){
            mRecordList.add(0, recordInfo);
            signAdapter.notifyDataSetChanged();
            addNumber();
        }
    }

    class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<RecordInfo> signBeanList;
        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        public SignAdapter(Context context, List<RecordInfo> signBeanList) {
            this.signBeanList = signBeanList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_sign_main, viewGroup, false);
            return new ViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ViewHolder vh = (ViewHolder) viewHolder;

            RecordInfo recordInfo = signBeanList.get(i);

            Glide.with(getActivity()).load(recordInfo.getHeadPath()).asBitmap().into(vh.ivHead);
            vh.tvName.setText(recordInfo.getName());
            vh.tvTime.setText(dateFormat.format(recordInfo.getTime()));
        }

        @Override
        public int getItemCount() {
            return signBeanList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivHead;
            TextView tvName;
            TextView tvTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivHead = itemView.findViewById(R.id.iv_head);
                tvName = itemView.findViewById(R.id.tv_name);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }
}
