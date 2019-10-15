package com.yunbiao.yb_smart_meeting.activity.fragment.child.child;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FlowFragment extends BaseFragment {

    private GridView gvFlow;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public FlowFragment() {
    }

    public static FlowFragment newInstance(long meetId) {
        FlowFragment fragment = new FlowFragment();
        Bundle args = new Bundle();
        args.putLong("meetId", meetId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_flow;
    }

    @Override
    protected void initView() {
        gvFlow = find(R.id.gv_flow_grid);
    }

    @Override
    protected void initData() {
        long meetId = getArguments().getLong("meetId");
        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(meetId);
        gvFlow.setAdapter(new FlowAdapter(flowInfos));
    }

    class FlowAdapter extends BaseAdapter{
        private List<FlowInfo> datas;

        public FlowAdapter(List<FlowInfo> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
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

            if(getCount() == 1 && position == 0){
                viewHolder.lineUp.setVisibility(View.INVISIBLE);
                viewHolder.lineDown.setVisibility(View.INVISIBLE);
            } else if (position == 0) {
                viewHolder.lineUp.setVisibility(View.INVISIBLE);
            } else if (position >= getCount() - 1) {
                viewHolder.lineDown.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.lineUp.setVisibility(View.VISIBLE);
                viewHolder.lineDown.setVisibility(View.VISIBLE);
            }

            FlowInfo flowInfo = datas.get(position);
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

            if(position % 2 != 0){
                viewHolder.tvFlowName.setText(flowInfo.getBegin());
                viewHolder.tvFlowTime.setText(flowInfo.getName());
            } else {
                viewHolder.tvFlowName.setText(flowInfo.getName());
                viewHolder.tvFlowTime.setText(flowInfo.getBegin());
            }
            return convertView;
        }

        class ViewHolder {
            TextView tvFlowName;
            TextView tvFlowTime;
            View lineUp;
            View lineDown;
            ImageView ivCircle;
        }
    }
}
