package com.yunbiao.yb_smart_meeting.db2;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Objects;

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
    private String codeUrl;
    private int num;

    @Generated(hash = 1357483309)
    public MeetInfo(long id, String beginTime, String endTime, long meetRoomId, String meetRoomName, String name,
            String theme, String userName, String codeUrl, int num) {
        this.id = id;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.meetRoomId = meetRoomId;
        this.meetRoomName = meetRoomName;
        this.name = name;
        this.theme = theme;
        this.userName = userName;
        this.codeUrl = codeUrl;
        this.num = num;
    }

    @Generated(hash = 1707028029)
    public MeetInfo() {
    }

    @Override
    public String toString() {
        return "MeetInfo{" +
                "id=" + id +
                ", beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", meetRoomId=" + meetRoomId +
                ", meetRoomName='" + meetRoomName + '\'' +
                ", name='" + name + '\'' +
                ", theme='" + theme + '\'' +
                ", userName='" + userName + '\'' +
                ", codeUrl='" + codeUrl + '\'' +
                ", num=" + num +
                '}';
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetInfo meetInfo = (MeetInfo) o;
        return id == meetInfo.id &&
                meetRoomId == meetInfo.meetRoomId &&
                beginTime.equals(meetInfo.beginTime) &&
                endTime.equals(meetInfo.endTime) &&
                meetRoomName.equals(meetInfo.meetRoomName) &&
                name.equals(meetInfo.name) &&
                theme.equals(meetInfo.theme) &&
                userName.equals(meetInfo.userName) &&
                codeUrl.equals(meetInfo.codeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, beginTime, endTime, meetRoomId, meetRoomName, name, theme, userName, codeUrl);
    }
}