package com.yunbiao.yb_smart_meeting.activity.Event;

public class GetMeetingEvent {
    public static final int NO_MEETING = 0;
    public static final int GET_MEETING_FAILED = -1;
    public static final int COMPLETE = 1;

    private int state;

    public GetMeetingEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
