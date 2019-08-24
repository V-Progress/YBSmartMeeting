package com.yunbiao.yb_smart_meeting.activity.Event;

public class IntroduceUpdateEvent {
    boolean hasData = false;

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public IntroduceUpdateEvent(boolean hasData) {
        this.hasData = hasData;
    }
}
