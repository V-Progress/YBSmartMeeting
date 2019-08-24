package com.yunbiao.yb_smart_meeting.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AdvertInfo{
    @Id
    private long id;

    private long meetId;

    private int type;
    private String url;
    private int advertId;

    private String path;

    @Generated(hash = 1696909390)
    public AdvertInfo(long id, long meetId, int type, String url, int advertId,
            String path) {
        this.id = id;
        this.meetId = meetId;
        this.type = type;
        this.url = url;
        this.advertId = advertId;
        this.path = path;
    }

    @Generated(hash = 1146491101)
    public AdvertInfo() {
    }

    @Override
    public String toString() {
        return "AdvertInfo{" +
                "id=" + id +
                ", meetId=" + meetId +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", advertId=" + advertId +
                ", path='" + path + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMeetId() {
        return meetId;
    }

    public void setMeetId(long meetId) {
        this.meetId = meetId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAdvertId() {
        return advertId;
    }

    public void setAdvertId(int advertId) {
        this.advertId = advertId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}