package com.yunbiao.yb_smart_meeting.activity.Event;

import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.util.Objects;

public class MeetingEvent {
    public static final int GET_COMPLETE_SUCESS = 9;
    public static final int GET_MEETING_FAILED = -2;
    public static final int GET_NO_MEETING = -1;//没有会议

    public static final int LOAD_PRELOAD = 0;//预加载
    public static final int LOAD_BEGAN = 1;//已开始
    public static final int LOAD_ENDED = 2;//已结束

    private int state;
    private MeetInfo meetInfo;

    public MeetingEvent(int state) {
        this.state = state;
    }

    public MeetingEvent(int state, MeetInfo meetInfo) {
        this.state = state;
        this.meetInfo = meetInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public MeetInfo getMeetInfo() {
        return meetInfo;
    }

    public void setMeetInfo(MeetInfo meetInfo) {
        this.meetInfo = meetInfo;
    }

    @Override
    public String toString() {
        return "MeetingEvent{" +
                "state=" + state +
                ", meetInfo=" + meetInfo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingEvent that = (MeetingEvent) o;
        return state == that.state &&
                meetInfo.equals(that.meetInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, meetInfo);
    }
}
