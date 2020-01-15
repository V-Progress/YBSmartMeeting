package com.yunbiao.yb_smart_meeting.model.meet_model;

import java.util.List;

public class MeetingResponse {

    private String message;
    private int status;
    private List<Meet> meetArray;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Meet> getMeetArray() {
        return meetArray;
    }

    public void setMeetArray(List<Meet> meetArray) {
        this.meetArray = meetArray;
    }

    @Override
    public String toString() {
        return "MeetingResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", meetArray=" + meetArray +
                '}';
    }

}
