package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class RecordFragment extends BaseFragment {
    private static final String TAG = "RecordFragment";
    private View rootView;
    private RecyclerView recyclerView;
    private TextView tvAlready;
    private TextView tvShould;
    private SignAdapter signAdapter;
    private List<RecordInfo> mRecordList = new ArrayList<>();
    private View llPersonNumber;

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
        signAdapter = new SignAdapter();
        recyclerView.setAdapter(signAdapter);

        tvAlready = find(R.id.tv_already);
        tvShould = find(R.id.tv_should);
        llPersonNumber = find(R.id.ll_person_number);
    }

    @Override
    protected void initData() {
        showLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event) {
        int state = event.getState();
        MeetInfo meetInfo = event.getMeetInfo();
        switch (state) {
            case MeetingEvent.GET_MEETING_FAILED:
                showTips("加载失败");
                clearData();
                llPersonNumber.setVisibility(View.GONE);
                break;
            case MeetingEvent.GET_NO_MEETING:
                showTips("暂无会议");
                clearData();
                llPersonNumber.setVisibility(View.GONE);
                break;
            case MeetingEvent.GET_COMPLETE_SUCESS:
                llPersonNumber.setVisibility(View.VISIBLE);
                initData(meetInfo);
                break;
        }
    }

    private void clearData() {
        mRecordList.clear();
        signAdapter.notifyDataSetChanged();
        intNumber(-1);
    }

    private void initData(MeetInfo meetInfo) {
        hideLoadingAndTips();
        MeetInfo meetInfo1 = DaoManager.get().queryMeetInfoByNum(1);
        long id = meetInfo1.getId();

        List<RecordInfo> recordInfos = DaoManager.get().queryRecordByMeetId(id);
        mRecordList.clear();
        if (recordInfos != null) {
            mRecordList.addAll(removeDuplicate(recordInfos));
            Collections.sort(mRecordList, new Comparator<RecordInfo>() {
                @Override
                public int compare(RecordInfo o1, RecordInfo o2) {
                    //倒序：左大于右返回正值 //正序：左边大时返回负值
                    return o1.getTime() > o2.getTime() ? -1 : o1.getTime() < o2.getTime() ? 1 : 0;
                }
            });
        }
        signAdapter.notifyDataSetChanged();

        intNumber(id);
    }

    private Collection<RecordInfo> removeDuplicate(List<RecordInfo> list) {
        HashMap<String, RecordInfo> recordInfoMap = new HashMap<>();
        for (RecordInfo recordInfo : list) {
            String meetEntryId = recordInfo.getMeetEntryId();
            if (recordInfoMap.containsKey(meetEntryId)) {
                RecordInfo hasRecord = recordInfoMap.get(meetEntryId);
                long time1 = hasRecord.getTime();
                long time = recordInfo.getTime();
                if (time < time1) {
                    recordInfoMap.put(meetEntryId, recordInfo);
                }
            } else {
                recordInfoMap.put(meetEntryId, recordInfo);
            }
        }
        return recordInfoMap.values();
    }

    private void intNumber(long id) {
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
        tvShould.setText(entryInfos.size() + "");
        addNumber();
    }

    private void addNumber() {
        tvAlready.post(new Runnable() {
            @Override
            public void run() {
                tvAlready.setText(mRecordList.size() + "");
            }
        });
    }

    public void addRecord(final RecordInfo recordInfo) {
        String meetEntryId = recordInfo.getMeetEntryId();

        boolean isContains = false;
        for (int i = 0; i < mRecordList.size(); i++) {
            String id = mRecordList.get(i).getMeetEntryId();
            if (TextUtils.equals(meetEntryId, id)) {
                isContains = true;
            }
        }

        if (!isContains) {
            mRecordList.add(0, recordInfo);
            signAdapter.notifyDataSetChanged();
            addNumber();
        }
    }

    class SignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        public SignAdapter() {
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
            vh.bindData(mRecordList.get(i));
        }

        @Override
        public int getItemCount() {
            return mRecordList.size();
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

            private void bindData(RecordInfo recordInfo) {
                //设置图片圆角角度
                Glide.with(getActivity())
                        .load(/*recordInfo.getImageBytes() != null ? recordInfo.getImageBytes() : */recordInfo.getHeadPath())
                        .asBitmap()
                        .transform(new RoundedCornersTransformation(getActivity(), 10, 5))
                        .override(100, 100)
                        .into(ivHead);
                tvName.setText(recordInfo.getName());
                tvTime.setText(dateFormat.format(recordInfo.getTime()));
            }
        }
    }
}
