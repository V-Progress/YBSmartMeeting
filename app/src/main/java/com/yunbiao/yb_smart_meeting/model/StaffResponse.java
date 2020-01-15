package com.yunbiao.yb_smart_meeting.model;

import com.yunbiao.yb_smart_meeting.db2.DepartBean;

import java.util.List;

/**
 * Created by Administrator on 2018/10/18.
 */

public class StaffResponse {

    private List<DepartBean> dep;
    private int status;

    @Override
    public String toString() {
        return "StaffResponse{" +
                "dep=" + dep +
                ", status=" + status +
                '}';
    }

    public List<DepartBean> getDep() {
        return dep;
    }

    public void setDep(List<DepartBean> dep) {
        this.dep = dep;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
