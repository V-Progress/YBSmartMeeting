package com.yunbiao.yb_smart_meeting.model.meet_model;


import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.util.List;

public class Meet {

    private List<AdvertInfo> advertArray;
    private List<EntryInfo> entryArray;
    private List<FlowInfo> flowArray;
    private MeetInfo meetInfo;

    @Override
    public String toString() {
        return "Meet{" +
                "advertArray=" + advertArray +
                ", entryArray=" + entryArray +
                ", flowArray=" + flowArray +
                ", meetInfo=" + meetInfo +
                '}';
    }

    public List<AdvertInfo> getAdvertArray() {
        return advertArray;
    }

    public void setAdvertArray(List<AdvertInfo> advertArray) {
        this.advertArray = advertArray;
    }

    public List<EntryInfo> getEntryArray() {
        return entryArray;
    }

    public void setEntryArray(List<EntryInfo> entryArray) {
        this.entryArray = entryArray;
    }

    public List<FlowInfo> getFlowArray() {
        return flowArray;
    }

    public void setFlowArray(List<FlowInfo> flowArray) {
        this.flowArray = flowArray;
    }

    public MeetInfo getMeetInfo() {
        return meetInfo;
    }

    public void setMeetInfo(MeetInfo meetInfo) {
        this.meetInfo = meetInfo;
    }
}
