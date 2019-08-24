package com.yunbiao.yb_smart_meeting.bean.meet_model;


import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import java.util.List;

public class Meet {

    private List<AdvertInfo> advertArray;
    private List<EntryInfo> entryInfoArray;
    private List<FlowInfo> flowInfoArray;
    private MeetInfo meetInfo;

    @Override
    public String toString() {
        return "Meet{" +
                "advertArray=" + advertArray +
                ", entryInfoArray=" + entryInfoArray +
                ", flowInfoArray=" + flowInfoArray +
                ", meetInfo=" + meetInfo +
                '}';
    }

    public List<AdvertInfo> getAdvertArray() {
        return advertArray;
    }

    public void setAdvertArray(List<AdvertInfo> advertArray) {
        this.advertArray = advertArray;
    }

    public List<EntryInfo> getEntryInfoArray() {
        return entryInfoArray;
    }

    public void setEntryInfoArray(List<EntryInfo> entryInfoArray) {
        this.entryInfoArray = entryInfoArray;
    }

    public List<FlowInfo> getFlowInfoArray() {
        return flowInfoArray;
    }

    public void setFlowInfoArray(List<FlowInfo> flowInfoArray) {
        this.flowInfoArray = flowInfoArray;
    }

    public MeetInfo getMeetInfo() {
        return meetInfo;
    }

    public void setMeetInfo(MeetInfo meetInfo) {
        this.meetInfo = meetInfo;
    }
}
