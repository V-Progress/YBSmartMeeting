package com.yunbiao.yb_smart_meeting.activity.Event;

import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

public class MeetingEvent {
    public static final int NO_MEETING = -1;
    public static final int INIT = 0;
    public static final int PRELOADING = 1;
    public static final int BEGINED = 2;
    public static final int END = 3;

    private int state;
    private MeetInfo meetInfo;

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
}
