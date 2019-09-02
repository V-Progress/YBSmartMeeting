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

    private long readNum;
    private long goodNum;

    private int time;

    private String shareUrl;

    @Generated(hash = 1237662108)
    public AdvertInfo(long id, long meetId, int type, String url, int advertId,
            String path, long readNum, long goodNum, int time, String shareUrl) {
        this.id = id;
        this.meetId = meetId;
        this.type = type;
        this.url = url;
        this.advertId = advertId;
        this.path = path;
        this.readNum = readNum;
        this.goodNum = goodNum;
        this.time = time;
        this.shareUrl = shareUrl;
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
                ", readNum=" + readNum +
                ", goodNum=" + goodNum +
                ", time=" + time +
                ", shareUrl='" + shareUrl + '\'' +
                '}';
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getReadNum() {
        return readNum;
    }

    public void setReadNum(long readNum) {
        this.readNum = readNum;
    }

    public long getGoodNum() {
        return goodNum;
    }

    public void setGoodNum(long goodNum) {
        this.goodNum = goodNum;
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