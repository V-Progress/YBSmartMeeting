package com.yunbiao.yb_smart_meeting.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {@Index(value = "meetId DESC,  meetEntryId DESC", unique = true)})
public class EntryInfo {
    @Id(autoincrement = true)
    private Long id;

    private long meetId;

    private long comId;

    private String meetEntryId;

    private int sex;
    private String phone;
    private String tectitle;
    private String comName;
    private String name;
    private String seatNumber;
    private int type;
    private String head;

    private String headPath;

    @Generated(hash = 1817627997)
    public EntryInfo(Long id, long meetId, long comId, String meetEntryId, int sex,
            String phone, String tectitle, String comName, String name,
            String seatNumber, int type, String head, String headPath) {
        this.id = id;
        this.meetId = meetId;
        this.comId = comId;
        this.meetEntryId = meetEntryId;
        this.sex = sex;
        this.phone = phone;
        this.tectitle = tectitle;
        this.comName = comName;
        this.name = name;
        this.seatNumber = seatNumber;
        this.type = type;
        this.head = head;
        this.headPath = headPath;
    }

    @Generated(hash = 474082604)
    public EntryInfo() {
    }

    @Override
    public String toString() {
        return "EntryInfo{" +
                "id=" + id +
                ", meetId=" + meetId +
                ", comId=" + comId +
                ", meetEntryId='" + meetEntryId + '\'' +
                ", sex=" + sex +
                ", phone='" + phone + '\'' +
                ", tectitle='" + tectitle + '\'' +
                ", comName='" + comName + '\'' +
                ", name='" + name + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", type=" + type +
                ", head='" + head + '\'' +
                ", headPath='" + headPath + '\'' +
                '}';
    }

    public long getComId() {
        return comId;
    }

    public void setComId(long comId) {
        this.comId = comId;
    }

    public String getMeetEntryId() {
        return meetEntryId;
    }

    public void setMeetEntryId(String meetEntryId) {
        this.meetEntryId = meetEntryId;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public long getMeetId() {
        return meetId;
    }

    public void setMeetId(long meetId) {
        this.meetId = meetId;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTectitle() {
        return tectitle;
    }

    public void setTectitle(String tectitle) {
        this.tectitle = tectitle;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
}
