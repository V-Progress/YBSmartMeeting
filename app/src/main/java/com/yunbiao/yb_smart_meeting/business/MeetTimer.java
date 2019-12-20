package com.yunbiao.yb_smart_meeting.business;

import android.support.annotation.NonNull;

import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MeetTimer {
    private static MeetTimer meetTimer = new MeetTimer();
    private static DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final int STATE_BEFORE = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_ING = 2;
    public static final int STATE_END = -1;
    private MeetTimeCallback mCallback = null;

    public static MeetTimer getInstance() {
        return meetTimer;
    }

    private Timer timer;

    public interface MeetTimeCallback {
        void onState(MeetInfo meetInfo, int state);

        void onError(Throwable t);
    }

    public void checkTime(final MeetInfo meetInfo, @NonNull MeetTimeCallback callback) {
        mCallback = callback;
        String beginTime = meetInfo.getBeginTime();
        String endTime = meetInfo.getEndTime();
        try {
            Date begin = formater.parse(beginTime);
            Date end = formater.parse(endTime);
            Date ready = new Date(begin.getTime() - 600000);//提前10分钟进场
            Date currDate = new Date();

            int state = 0;
            if (currDate.before(begin)) {//会议未开始
                if (currDate.before(ready)) {//会议在准备之前
                    state = STATE_BEFORE;
                } else {//会议即将开始
                    state = STATE_READY;
                }
            } else if (currDate.after(end)) {//会议已结束
                state = STATE_END;
            } else {//会议正在进行
                state = STATE_ING;
            }
            callback.onState(meetInfo, state);

            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            if (state == STATE_BEFORE) {//如果状态是未开始，添加三个定时（准备，开始，结束）
                setTime(timer, ready, meetInfo, STATE_READY);
                setTime(timer, begin, meetInfo, STATE_ING);
                setTime(timer, end, meetInfo, STATE_END);
            } else if (state == STATE_READY) {//如果状态是准备，添加两个定时（开始，结束）
                setTime(timer, begin, meetInfo, STATE_ING);
                setTime(timer, end, meetInfo, STATE_END);
            } else if (state == STATE_ING) {//如果状态是正在进行，添加一个定时（结束）
                setTime(timer, end, meetInfo, STATE_END);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    private void setTime(Timer time, Date date, final MeetInfo meetInfo, final int state) {
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                mCallback.onState(meetInfo, state);
            }
        }, date);
    }
}
