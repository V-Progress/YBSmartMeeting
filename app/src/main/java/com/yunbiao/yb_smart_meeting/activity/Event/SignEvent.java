package com.yunbiao.yb_smart_meeting.activity.Event;

import com.yunbiao.yb_smart_meeting.db2.EntryInfo;

public class SignEvent {
    EntryInfo entryInfo;
    boolean isSigned;

    public EntryInfo getEntryInfo() {
        return entryInfo;
    }

    public void setEntryInfo(EntryInfo entryInfo) {
        this.entryInfo = entryInfo;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }
}
