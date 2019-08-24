package com.yunbiao.yb_smart_meeting.bean.meet_model;


import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;

import java.util.List;

public class Advert{
    private int time;
    private List<AdvertInfo> advert;

    @Override
    public String toString() {
        return "Advert{" +
                "time=" + time +
                ", advert=" + advert +
                '}';
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<AdvertInfo> getAdvert() {
        return advert;
    }

    public void setAdvert(List<AdvertInfo> advert) {
        this.advert = advert;
    }
}
