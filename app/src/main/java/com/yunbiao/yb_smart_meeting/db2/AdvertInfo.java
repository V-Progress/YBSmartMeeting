package com.yunbiao.yb_smart_meeting.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

@Entity(indexes = {@Index(value = "meetId DESC,  advertId DESC", unique = true)})
public class AdvertInfo {
    @Id(autoincrement = true)
    private Long id;

    private long meetId;

    private long comId;

    private int type;
    private String url;
    private int advertId;

    private String path;

    private long readNum;
    private long goodNum;

    private int time;

    private String shareUrl;

    @Generated(hash = 1414240548)
    public AdvertInfo(Long id, long meetId, long comId, int type, String url,
            int advertId, String path, long readNum, long goodNum, int time,
            String shareUrl) {
        this.id = id;
        this.meetId = meetId;
        this.comId = comId;
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
                ", comId=" + comId +
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

    public long getComId() {
        return comId;
    }

    public void setComId(long comId) {
        this.comId = comId;
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

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }
}