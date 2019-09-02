package com.yunbiao.yb_smart_meeting.business;

import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MeetingLoader {
    private static final String TAG = "MeetingLoader";
    private static MeetingLoader meetingLoader = new MeetingLoader();
    private DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private MeetingProcess meetingProcess = new MeetingProcess();
    private Timer timer = null;

    public static MeetingLoader i() {
        return meetingLoader;
    }

    public void update() {
    }

    /***
     * 加载当前会议或最近的会议
     */
    public void loadCurrentMeeting() {
        MeetInfo currentMeetInfo = meetingProcess.getCurrentMeetInfo();
        Date beginDate = meetingProcess.getBeginDate();
        Date endDate = meetingProcess.getEndDate();
        //判断如果当前有正在进行的会议则不刷新，等会议结束后再刷新
        if(currentMeetInfo != null && beginDate != null && endDate != null){
            Date date = new Date();
            if(date.after(beginDate) && date.before(endDate)){
                return;
            }
        }

        List<MeetInfo> meetInfos = DaoManager.get().queryAll(MeetInfo.class);
        if(meetInfos.size()<=0){
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.NO_MEETING,meetingProcess.getCurrentMeetInfo()));
            return;
        }

        Date tempBeginDate = null;
        for (final MeetInfo meetInfo : meetInfos) {
            Date begin = null;
            Date end = null;
            try {
                begin = formater.parse(meetInfo.getBeginTime());
                end = formater.parse(meetInfo.getEndTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //如果没有正在进行的会议，则判断
            if (tempBeginDate == null) {
                tempBeginDate = begin;
                meetingProcess.setMeet(meetInfo,begin,end);
            } else if (tempBeginDate.after(begin)) {
                tempBeginDate = begin;
                meetingProcess.setMeet(meetInfo,begin,end);
            }
        }

        if(timer != null){
            timer.cancel();
            timer = null;
        }

        Date currDate = new Date();
        if(currDate.after(meetingProcess.endDate)){
            DaoManager.get().delete(meetingProcess.currentMeetInfo);
            loadCurrentMeeting();
            return;
        }

        if(currDate.before(meetingProcess.beginDate)){
            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.PRELOAD,meetingProcess.getCurrentMeetInfo()));
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new MeetingEvent(MeetingEvent.BEGAN,meetingProcess.getCurrentMeetInfo()));
            }
        }, meetingProcess.getBeginDate());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new MeetingEvent(MeetingEvent.ENDED,meetingProcess.getCurrentMeetInfo()));
                meetingProcess.setMeet(null,null,null);

                MeetingManager.getInstance().getAllMeeting();
            }
        }, meetingProcess.getEndDate());
    }

    class MeetingProcess{
        private MeetInfo currentMeetInfo;
        private Date beginDate;
        private Date endDate;

        public void setMeet(MeetInfo meet,Date bDate,Date eDate){
            currentMeetInfo = meet;
            beginDate = bDate;
            endDate = eDate;
        }

        public MeetInfo getCurrentMeetInfo() {
            return currentMeetInfo;
        }

        public Date getBeginDate() {
            return beginDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        @Override
        public String toString() {
            return "MeetingProcess{" +
                    "currentMeetInfo=" + currentMeetInfo +
                    ", beginDate=" + beginDate +
                    ", endDate=" + endDate +
                    '}';
        }
    }
}
