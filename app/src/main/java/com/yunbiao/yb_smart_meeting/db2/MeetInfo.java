package com.yunbiao.yb_smart_meeting.db2;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class MeetInfo {
    @Id
    @Unique
    private long id;

    private String beginTime;
    private String endTime;
    private long meetRoomId;
    private String meetRoomName;
    private String name;
    private String theme;
    private String userName;

    @Generated(hash = 836387009)
    public MeetInfo(long id, String beginTime, String endTime, long meetRoomId,
            String meetRoomName, String name, String theme, String userName) {
        this.id = id;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.meetRoomId = meetRoomId;
        this.meetRoomName = meetRoomName;
        this.name = name;
        this.theme = theme;
        this.userName = userName;
    }

    @Generated(hash = 1707028029)
    public MeetInfo() {
    }

    @Override
    public String toString() {
        return "MeetInfo{" +
                "beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", id=" + id +
                ", meetRoomId=" + meetRoomId +
                ", meetRoomName='" + meetRoomName + '\'' +
                ", name='" + name + '\'' +
                ", theme='" + theme + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMeetRoomId() {
        return meetRoomId;
    }

    public void setMeetRoomId(long meetRoomId) {
        this.meetRoomId = meetRoomId;
    }

    public String getMeetRoomName() {
        return meetRoomName;
    }

    public void setMeetRoomName(String meetRoomName) {
        this.meetRoomName = meetRoomName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}