package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MeetingListFragment extends BaseFragment {

    private static final String TAG = "MeetingListFragment";
    private RecyclerView rlvMeetingList;

    public static MeetingListFragment instance(String key, long meetId){
        MeetingListFragment meetingListFragment = new MeetingListFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(key,meetId);
        meetingListFragment.setArguments(bundle);
        return meetingListFragment;
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_meeting_list;
    }

    @Override
    protected void initView() {
        rlvMeetingList = find(R.id.rlv_meeting_list);
        rlvMeetingList.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.VERTICAL, false));
    }

    @Override
    protected void initData() {
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        List<MeetInfo> meetInfoList = DaoManager.get().queryMeetInfoByComId(comId);
        Collections.sort(meetInfoList, new Comparator<MeetInfo>() {
            @Override
            public int compare(MeetInfo o1, MeetInfo o2) {
                //倒序：左大于右返回正值 //正序：左边大时返回负值
                return o1.getNum() < o2.getNum() ? -1 : o1.getNum() > o2.getNum() ? 1 : 0;
            }
        });
        rlvMeetingList.setAdapter(new MeetingAdapter(meetInfoList));
    }

    class MeetingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<MeetInfo> list;

        public MeetingAdapter(List<MeetInfo> meetInfos) {
            list = meetInfos;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new VH(LayoutInflater.from(getActivity()).inflate(R.layout.item_meeting, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            MeetInfo meetInfo = list.get(i);
            ((VH) viewHolder).bindData(meetInfo, i);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            private final TextView name;
            private final TextView number;
            private final TextView begin;
            private final TextView end;
            private final View rootView;

            public VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_meeting_name);
                number = itemView.findViewById(R.id.tv_meeting_number);
                begin = itemView.findViewById(R.id.tv_meeting_begin);
                end = itemView.findViewById(R.id.tv_meeting_end);
                rootView = itemView.findViewById(R.id.ll_root);
            }

            public void bindData(MeetInfo meetInfo, int i) {
                if (i % 2 == 0) {
                    rootView.setBackgroundColor(Color.parseColor("#051C60"));
                } else {
                    rootView.setBackgroundColor(Color.parseColor("#156094"));
                }
                List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetInfo.getId());
                int total = entryInfos == null ? 0 : entryInfos.size();
                number.setText(String.valueOf(total));
                name.setText(meetInfo.getName());
                begin.setText(meetInfo.getBeginTime());
                end.setText(meetInfo.getEndTime());
            }
        }
    }
}
