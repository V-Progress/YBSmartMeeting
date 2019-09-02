package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.List;

public class RecordFragment extends BaseFragment {
    private static final String TAG = "RecordFragment";
    //    private MeetInfo mMeetInfo;
    private View rootView;
    private RecyclerView recyclerView;
    private TextView tvAlready;
    private TextView tvShould;
    private List<RecordInfo> mRecordList = new ArrayList<>();
    private SignAdapter signAdapter;
    private List<Long> currNumberList = new ArrayList<>();

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
        Log.e(TAG, "update: 收到会议更新事件");
        int state = event.getState();
        switch (state) {
            case MeetingEvent.GET_MEETING_FAILED:
            case MeetingEvent.NO_MEETING:
                showTips("暂无会议");
                break;
            case MeetingEvent.PRELOAD:
            case MeetingEvent.BEGAN:
                initData(event.getMeetInfo());
                break;
        }
    }

    private void initData(MeetInfo meetInfo) {
        long id = meetInfo.getId();

        List<RecordInfo> recordInfos = DaoManager.get().queryRecordByMeetId(id);
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
        Collections.sort(recordInfos, new Comparator<RecordInfo>() {
            @Override
            public int compare(RecordInfo o1, RecordInfo o2) {
                //倒序：左大于右返回正值
                return o1.getTime() < o2.getTime() ? 1 : o1.getTime() > o2.getTime() ? -1 : 0;
            }
        });

        currNumberList.clear();
        mRecordList.clear();
        if (recordInfos != null) {
            mRecordList.addAll(recordInfos);
        }
        signAdapter.notifyDataSetChanged();

        hideLoadingAndTips();

        intNumber(recordInfos, entryInfos);
    }

    private void intNumber(List<RecordInfo> recordInfos, List<EntryInfo> entryInfos) {
        int size = recordInfos == null ? 0 : recordInfos.size();
        int total = entryInfos == null ? 0 : entryInfos.size();
        Log.e(TAG, "initData: ---- " + size + " --- " + total);

        if (recordInfos != null) {
            for (RecordInfo recordInfo : recordInfos) {
                addNumber(recordInfo);
            }
        }
        tvShould.setText(entryInfos.size() + "");
    }

    private void addNumber(RecordInfo recordInfo) {
        if (!currNumberList.contains(recordInfo.getMeetEntryId())) {
            currNumberList.add(recordInfo.getMeetEntryId());
            tvAlready.setText(currNumberList.size() + "");
        }
    }

    public void addRecord(final RecordInfo recordInfo) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordList.add(0, recordInfo);
                signAdapter.notifyDataSetChanged();
                addNumber(recordInfo);
            }
        });
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
