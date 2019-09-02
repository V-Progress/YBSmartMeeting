package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class CurrFragment extends BaseFragment {
    private static final String TAG = "CurrFragment";
    private TextView tvCurrName;
    private TextView tvCurrTime;
    private TextView tvCurrState;
    private RecyclerView recyclerView;
    private View llContainer;

    @Override
    protected int setLayout() {
        return R.layout.fragment_curr;
    }

    @Override
    protected void initView() {
        tvCurrName = find(R.id.tv_curr_meeting_name);
        tvCurrTime = find(R.id.tv_curr_meeting_time);
        tvCurrState = find(R.id.tv_curr_meeting_state);
        recyclerView = find(R.id.rlv_info);
        llContainer = find(R.id.ll_meeting_info_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.VERTICAL, false));
    }

    @Override
    protected void initData() {
        showLoading();
        llContainer.setVisibility(View.GONE);
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

    private void loadMeetingInfo(MeetInfo meetInfo) {
        if (meetInfo == null) {
            showTips("暂无会议");
            recyclerView.setAdapter(new InfoAdapter(getActivity(), new ArrayList<InfoBean>()));
            return;
        }

        hideLoadingAndTips();
        llContainer.setVisibility(View.VISIBLE);
        d("加载参会人员");
        long id = meetInfo.getId();
        List<InfoBean> infoList = new ArrayList<>();

        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
        if (entryInfos != null && entryInfos.size() > 0) {
            int type;

            //添加嘉宾----------------------------------------------
            type = 3;
            List<EntryInfo> guests = new ArrayList<>();
            for (EntryInfo entryInfo : entryInfos) {
                if (entryInfo.getType() == type)
                    guests.add(entryInfo);
            }
            if (guests.size() > 0) {
                InfoBean infoBean = new InfoBean();
                infoBean.setType(type);
                infoBean.setEntryInfos(guests);
                infoList.add(infoBean);
            }

            //添加内部参会人员----------------------------------------
            type = 1;
            List<EntryInfo> entrys = new ArrayList<>();
            for (EntryInfo entryInfo : entryInfos) {
                if (entryInfo.getType() == type)
                    entrys.add(entryInfo);
            }
            if (entrys.size() > 0) {
                InfoBean infoBean = new InfoBean();
                infoBean.setType(type);
                infoBean.setEntryInfos(entrys);
                infoList.add(infoBean);
            }

            //添加外部参会人员----------------------------------------
            type = 2;
            List<EntryInfo> visitors = new ArrayList<>();
            for (EntryInfo entryInfo : entryInfos) {
                if (entryInfo.getType() == type)
                    visitors.add(entryInfo);
            }
            if (visitors.size() > 0) {
                InfoBean infoBean = new InfoBean();
                infoBean.setType(type);
                infoBean.setEntryInfos(visitors);
                infoList.add(infoBean);
            }
        }

        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(id);
        if (flowInfos != null && flowInfos.size() > 0) {
            InfoBean infoBean = new InfoBean();
            infoBean.setType(0);
            infoBean.setFlowInfos(flowInfos);
            infoList.add(infoBean);
        }

        recyclerView.setAdapter(new InfoAdapter(getActivity(), infoList));
    }

    class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final LayoutInflater mLayoutInflater;
        private List<InfoBean> list;

        public InfoAdapter(Context context, List<InfoBean> list) {
            mLayoutInflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            Log.e(TAG, "onCreateViewHolder: " + viewType);
            if (viewType > 0) {
                return new EntryViewHolder(mLayoutInflater.inflate(R.layout.layout_entry_grid, viewGroup, false));
            } else {
                return new FlowViewHolder(mLayoutInflater.inflate(R.layout.layout_flow_list, viewGroup, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int viewType = getItemViewType(i);
            if (viewType > 0) {
                ((EntryViewHolder) viewHolder).bindHolder(list.get(i));
            } else {
                ((FlowViewHolder) viewHolder).bindHolder(list.get(i));
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class EntryViewHolder extends RecyclerView.ViewHolder {

            private final GridView gv;
            private final TextView tvTitle;

            public EntryViewHolder(@NonNull View itemView) {
                super(itemView);
                gv = itemView.findViewById(R.id.gv_entry_grid);
                tvTitle = itemView.findViewById(R.id.tv_title);
            }

            public void bindHolder(InfoBean infoBean) {
                if (infoBean.getType() == 1) {
                    tvTitle.setText("内部参会人员");
                } else if (infoBean.getType() == 2) {
                    tvTitle.setText("外部参会人员");
                } else {
                    tvTitle.setText("参会嘉宾");
                }

                final List<EntryInfo> entryInfos = infoBean.getEntryInfos();
                gv.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return entryInfos.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return entryInfos.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        ViewHodler viewHodler;
                        if (convertView == null) {
                            viewHodler = new ViewHodler();
                            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry_info, parent, false);
                            viewHodler.civ = convertView.findViewById(R.id.civ_head);
                            convertView.setTag(viewHodler);
                        } else {
                            viewHodler = (ViewHodler) convertView.getTag();
                        }
                        EntryInfo entryInfo = entryInfos.get(position);
                        Glide.with(getActivity()).load(entryInfo.getHeadPath()).asBitmap().into(viewHodler.civ);
                        return convertView;
                    }

                    class ViewHodler {
                        CircleImageView civ;
                    }
                });
            }
        }

        class FlowViewHolder extends RecyclerView.ViewHolder {

            private final GridView gv;
            private final TextView tvTitle;
            private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            public FlowViewHolder(@NonNull View itemView) {
                super(itemView);
                gv = itemView.findViewById(R.id.gv_entry_grid);
                tvTitle = itemView.findViewById(R.id.tv_title);
            }

            public void bindHolder(InfoBean infoBean) {
                final List<FlowInfo> flowInfos = infoBean.getFlowInfos();
                tvTitle.setText("会议流程");
                gv.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return flowInfos.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return flowInfos.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final ViewHolder viewHolder;
                        if (convertView == null) {
                            viewHolder = new ViewHolder();
                            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flow_info, parent, false);
                            viewHolder.tvFlowName = convertView.findViewById(R.id.tv_flow_name);
                            viewHolder.tvFlowTime = convertView.findViewById(R.id.tv_flow_time);
                            viewHolder.lineUp = convertView.findViewById(R.id.line_up);
                            viewHolder.lineDown = convertView.findViewById(R.id.line_down);
                            viewHolder.ivCircle = convertView.findViewById(R.id.iv_circle);
                            convertView.setTag(viewHolder);
                        } else {
                            viewHolder = (ViewHolder) convertView.getTag();
                        }

                        if (position == 0) {
                            viewHolder.lineUp.setVisibility(View.INVISIBLE);
                        } else if (position >= getCount() - 1) {
                            viewHolder.lineDown.setVisibility(View.INVISIBLE);
                        } else {
                            viewHolder.lineUp.setVisibility(View.VISIBLE);
                            viewHolder.lineDown.setVisibility(View.VISIBLE);
                        }

                        FlowInfo flowInfo = flowInfos.get(position);
                        Date currDate = new Date();
                        Date beginDate = null;
                        Date endDate = null;
                        try {
                            beginDate = dateFormat.parse(flowInfo.getBegin());
                            endDate = dateFormat.parse(flowInfo.getEnd());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (beginDate != null && endDate != null) {
                            if (currDate.after(beginDate) && currDate.before(endDate)) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.tvFlowTime.setTextColor(Color.GREEN);
                                        viewHolder.tvFlowName.setTextColor(Color.GREEN);
                                        viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_ing);
                                    }
                                });
                            } else if (currDate.after(endDate)) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.tvFlowTime.setTextColor(Color.LTGRAY);
                                        viewHolder.tvFlowName.setTextColor(Color.LTGRAY);
                                        viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_no);
                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.tvFlowTime.setTextColor(Color.WHITE);
                                        viewHolder.tvFlowName.setTextColor(Color.WHITE);
                                        viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_normal);
                                    }
                                });
                            }

                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            viewHolder.tvFlowTime.setTextColor(Color.GREEN);
                                            viewHolder.tvFlowName.setTextColor(Color.GREEN);
                                            viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_ing);
                                        }
                                    });
                                }
                            }, beginDate);
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            viewHolder.tvFlowTime.setTextColor(Color.LTGRAY);
                                            viewHolder.tvFlowName.setTextColor(Color.LTGRAY);
                                            viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_no);
                                        }
                                    });
                                }
                            }, endDate);
                        }

                        viewHolder.tvFlowName.setText(flowInfo.getName());
                        viewHolder.tvFlowTime.setText(flowInfo.getBegin());
                        return convertView;
                    }

                    class ViewHolder {
                        TextView tvFlowName;
                        TextView tvFlowTime;
                        View lineUp;
                        View lineDown;
                        ImageView ivCircle;
                    }
                });
            }
        }
    }

    class InfoBean {
        int type;
        private List<EntryInfo> entryInfos;
        private List<FlowInfo> flowInfos;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<EntryInfo> getEntryInfos() {
            return entryInfos;
        }

        public void setEntryInfos(List<EntryInfo> entryInfos) {
            this.entryInfos = entryInfos;
        }

        public List<FlowInfo> getFlowInfos() {
            return flowInfos;
        }

        public void setFlowInfos(List<FlowInfo> flowInfos) {
            this.flowInfos = flowInfos;
        }

        @Override
        public String toString() {
            return "InfoBean{" +
                    "type=" + type +
                    ", entryInfos=" + entryInfos +
                    ", flowInfos=" + flowInfos +
                    '}';
        }
    }

}
