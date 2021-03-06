package com.yunbiao.yb_smart_meeting.model;

public class XmppLoginMessage {
    private Content content;
    private String sid;
    private int type;

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "XmppLoginMessage{" +
                "content=" + content +
                ", sid='" + sid + '\'' +
                ", type=" + type +
                '}';
    }

    public static class Content {
        private int bindStatus;
        private String deviceName;
        private String deviceQrCode;
        private int deviceVer;
        private int dtype;
        private String expireDate;
        private int isMirror;
        private String pwd;
        private String runKey;
        private int runStatus;
        private String serNum;
        private String status;
        private double voice;
        private int showType;
        private int showValue;
        private int flag;
        private int restart;

        public int getRestart() {
            return restart;
        }

        public void setRestart(int restart) {
            this.restart = restart;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public int getShowValue() {
            return showValue;
        }

        public void setShowValue(int showValue) {
            this.showValue = showValue;
        }

        public int getShowType() {
            return showType;
        }

        public void setShowType(int showType) {
            this.showType = showType;
        }

        public double getVoice() {
            return voice;
        }

        public void setVoice(double voice) {
            this.voice = voice;
        }

        public int getBindStatus() {
            return bindStatus;
        }

        public void setBindStatus(int bindStatus) {
            this.bindStatus = bindStatus;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceQrCode() {
            return deviceQrCode;
        }

        public void setDeviceQrCode(String deviceQrCode) {
            this.deviceQrCode = deviceQrCode;
        }

        public int getDeviceVer() {
            return deviceVer;
        }

        public void setDeviceVer(int deviceVer) {
            this.deviceVer = deviceVer;
        }

        public int getDtype() {
            return dtype;
        }

        public void setDtype(int dtype) {
            this.dtype = dtype;
        }

        public String getExpireDate() {
            return expireDate;
        }

        public void setExpireDate(String expireDate) {
            this.expireDate = expireDate;
        }

        public int getIsMirror() {
            return isMirror;
        }

        public void setIsMirror(int isMirror) {
            this.isMirror = isMirror;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getRunKey() {
            return runKey;
        }

        public void setRunKey(String runKey) {
            this.runKey = runKey;
        }

        public int getRunStatus() {
            return runStatus;
        }

        public void setRunStatus(int runStatus) {
            this.runStatus = runStatus;
        }

        public String getSerNum() {
            return serNum;
        }

        public void setSerNum(String serNum) {
            this.serNum = serNum;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
