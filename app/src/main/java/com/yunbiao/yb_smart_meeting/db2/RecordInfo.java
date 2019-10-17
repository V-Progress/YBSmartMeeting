package com.yunbiao.yb_smart_meeting.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Arrays;

@Entity
public class RecordInfo {

    @Id
    private Long id;

    private long meetEntryId;

    private long meetId;

    private String headPath;

    private boolean isUpload;

    private long time;

    private int type;

    private String name;

    private int smilar;

    @Transient
    private byte[] imageBytes;

    @Generated(hash = 2004673918)
    public RecordInfo(Long id, long meetEntryId, long meetId, String headPath,
            boolean isUpload, long time, int type, String name, int smilar) {
        this.id = id;
        this.meetEntryId = meetEntryId;
        this.meetId = meetId;
        this.headPath = headPath;
        this.isUpload = isUpload;
        this.time = time;
        this.type = type;
        this.name = name;
        this.smilar = smilar;
    }

    @Generated(hash = 1863816245)
    public RecordInfo() {
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                "id=" + id +
                ", meetEntryId=" + meetEntryId +
                ", meetId=" + meetId +
                ", headPath='" + headPath + '\'' +
                ", isUpload=" + isUpload +
                ", time=" + time +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", smilar=" + smilar +
                ", imageBytes=" + Arrays.toString(imageBytes) +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMeetEntryId() {
        return meetEntryId;
    }

    public void setMeetEntryId(long meetEntryId) {
        this.meetEntryId = meetEntryId;
    }

    public long getMeetId() {
        return meetId;
    }

    public void setMeetId(long meetId) {
        this.meetId = meetId;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSmilar() {
        return smilar;
    }

    public void setSmilar(int smilar) {
        this.smilar = smilar;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public byte[] getImageBytes() {
        return this.imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}
