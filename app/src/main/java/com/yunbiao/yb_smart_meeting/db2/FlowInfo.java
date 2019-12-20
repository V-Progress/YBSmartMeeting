package com.yunbiao.yb_smart_meeting.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

@Entity(indexes = {@Index(value = "meetId DESC,  begin DESC", unique = true)})
public class FlowInfo {
    @Id(autoincrement = true)
    private Long id;

    private long meetId;

    private long comId;

    private String begin;
    private String end;
    private String name;

    @Generated(hash = 1160183316)
    public FlowInfo(Long id, long meetId, long comId, String begin, String end,
            String name) {
        this.id = id;
        this.meetId = meetId;
        this.comId = comId;
        this.begin = begin;
        this.end = end;
        this.name = name;
    }

    @Generated(hash = 1025658429)
    public FlowInfo() {
    }

    @Override
    public String toString() {
        return "FlowInfo{" +
                "id=" + id +
                ", meetId=" + meetId +
                ", comId=" + comId +
                ", begin='" + begin + '\'' +
                ", end='" + end + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public long getComId() {
        return comId;
    }

    public void setComId(long comId) {
        this.comId = comId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMeetId() {
        return meetId;
    }

    public void setMeetId(long meetId) {
        this.meetId = meetId;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
