package com.yunbiao.yb_smart_meeting.model.meet_model;


class Guest {
    private String tectitle;
    private String name;
    private int type;
    private int sex;
    private String head;
    private int meetEntryId;

    @Override
    public String toString() {
        return "Guest{" +
                "tectitle='" + tectitle + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", sex=" + sex +
                ", head='" + head + '\'' +
                ", meetEntryId=" + meetEntryId +
                '}';
    }

    public String getTectitle() {
        return tectitle;
    }

    public void setTectitle(String tectitle) {
        this.tectitle = tectitle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public int getMeetEntryId() {
        return meetEntryId;
    }

    public void setMeetEntryId(int meetEntryId) {
        this.meetEntryId = meetEntryId;
    }
}
