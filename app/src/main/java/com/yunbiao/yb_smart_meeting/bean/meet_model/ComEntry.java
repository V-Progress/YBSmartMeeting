package com.yunbiao.yb_smart_meeting.bean.meet_model;

public class ComEntry {
    private String comName;
    private String head;
    private String name;

    private int entryId;
    private int meetEntryId;
    private int peoples;
    private int sex;
    private int type;

    @Override
    public String toString() {
        return "ComEntry{" +
                "comName='" + comName + '\'' +
                ", head='" + head + '\'' +
                ", name='" + name + '\'' +
                ", entryId=" + entryId +
                ", meetEntryId=" + meetEntryId +
                ", peoples=" + peoples +
                ", sex=" + sex +
                ", type=" + type +
                '}';
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getMeetEntryId() {
        return meetEntryId;
    }

    public void setMeetEntryId(int meetEntryId) {
        this.meetEntryId = meetEntryId;
    }

    public int getPeoples() {
        return peoples;
    }

    public void setPeoples(int peoples) {
        this.peoples = peoples;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
