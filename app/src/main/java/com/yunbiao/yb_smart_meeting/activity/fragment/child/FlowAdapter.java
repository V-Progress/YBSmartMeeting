package com.yunbiao.yb_smart_meeting.activity.fragment.child;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class FlowAdapter extends BaseAdapter {
    private List<FlowInfo> datas;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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

        if (getCount() == 1 && position == 0) {
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
                viewHolder.tvFlowTime.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.tvFlowTime.setTextColor(Color.GREEN);
                        viewHolder.tvFlowName.setTextColor(Color.GREEN);
                        viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_ing);
                    }
                });
            } else if (currDate.after(endDate)) {
                viewHolder.tvFlowTime.post(new Runnable() {
                    @Override
                    public void run() {
                        viewHolder.tvFlowTime.setTextColor(Color.LTGRAY);
                        viewHolder.tvFlowName.setTextColor(Color.LTGRAY);
                        viewHolder.ivCircle.setImageResource(R.mipmap.timeline_circle_no);
                    }
                });
            } else {
                viewHolder.tvFlowTime.post(new Runnable() {
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
                    viewHolder.tvFlowTime.post(new Runnable() {
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
                    viewHolder.tvFlowTime.post(new Runnable() {
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

        if (position % 2 != 0) {
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