package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import com.yunbiao.yb_smart_meeting.db2.EntryInfo;

class SignModel {
    private boolean isSigned;
    private EntryInfo entryInfo;

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }

    public EntryInfo getEntryInfo() {
        return entryInfo;
    }

    public void setEntryInfo(EntryInfo entryInfo) {
        this.entryInfo = entryInfo;
    }
}