package com.yunbiao.yb_smart_meeting.business;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.afinel.PathManager;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.bean.meet_model.Meet;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Request;

public class DataLoader {
    private static final String TAG = "DataLoader";
    private static DataLoader meetingLoader = new DataLoader();
    private LoadMeetCallback callback;
    private boolean isAutoThreadRunning = false;
    private ScheduledExecutorService executor;

    private String mResponse;
    private String mError;

    private DataLoader() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public static DataLoader i() {
        return meetingLoader;
    }

    /***
     * 开始定时加载会议的任务
     * @param callback
     */
    public void startAutoGetMeeting(@NonNull final LoadMeetCallback callback) {
        this.callback = callback;
        d("是否正在运行：" + isAutoThreadRunning);
        if (isAutoThreadRunning) {
            return;
        }
        d("开始运行定时任务");
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                isAutoThreadRunning = true;
                requestMeeting();
            }
        }, 3, 20, TimeUnit.SECONDS);
    }

    /***
     * 请求会议
     */
    private void requestMeeting() {
        d("开始请求数据");
        d("地址：" + ResourceUpdate.GET_MEETING);
        d("参数：" + "deviceNo:" + HeartBeatClient.getDeviceNo());
        OkHttpUtils.post()
                .url(ResourceUpdate.GET_MEETING)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        callback.onStartGetMeeting();
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("获取失败：" + (e == null ? "NULL" : (e.getClass().getSimpleName() + ":" + e.getMessage())));
                        String error = e == null ? "UnknownException" : e.getMessage();
                        if (!TextUtils.isEmpty(mError) && TextUtils.equals(error, mError)) {
                            callback.notChange();
                        } else {
                            //会议缓存为空代表第一次就获取失败，外部需提示
                            if(TextUtils.isEmpty(mResponse)){
                                callback.onErrorGetMeeting(e);
                            } else {//会议缓存不为空代表会议更新失败，外部可提示可不提示
                                callback.onErrorUpdateMeeting(e);
                            }
                        }
                        mError = error;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d("获取成功：" + response);
                        MeetingResponse meetingResponse = new Gson().fromJson(response, MeetingResponse.class);
                        //成功的时候把Error置为空
                        mError = "";
                        //判断缓存中的response是否与请求的response相等，如果相等说明没有更新
                        if (!TextUtils.isEmpty(mResponse) && TextUtils.equals(mResponse, response)) {
                            callback.notChange();
                            return;
                        }

                        List<Meet> meetArray = meetingResponse.getMeetArray();
                        if (meetingResponse.getStatus() == 1) {
                            if (meetArray == null || meetArray.size() <= 0) {//没有会议
                                callback.noMeeting();
                            } else {
                                callback.onMeetingGeted(meetingResponse);
                            }
                        } else if (meetingResponse.getStatus() == 4) {//未绑定会议室
                            callback.notBind();
                        } else if (meetingResponse.getStatus() == 2) {//没有会议
                            callback.noMeeting();
                        } else {
                            callback.onErrorGetMeeting(new Exception(meetingResponse.getMessage()));
                        }
                        mResponse = response;
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    /***
     * 清除该公司下所有的会议和其他信息
     */
    public void clearDataByCurrCompany() {
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        List<MeetInfo> meetInfos = DaoManager.get().queryMeetByComId(comId);
        DaoManager.get().deleteMeetInfos(meetInfos);
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByComId(comId);
        DaoManager.get().deleteEntryInfos(entryInfos);
        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByComdId(comId);
        DaoManager.get().deleteAdvertInfos(advertInfos);
        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByComId(comId);
        DaoManager.get().deleteFlowInfos(flowInfos);
    }

    /***
     * 开始处理数据
     * @param meetingResponse
     */
    public void handleData(final MeetingResponse meetingResponse, @NonNull final HandleDataCallback callback) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                clearDataByCurrCompany();

                d("处理会议");
                int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
                //删除不存在的会议
                List<Meet> meetArray = meetingResponse.getMeetArray();
                d("handleData: 服务端会议总数：" + meetArray.size());

                //更新会议信息（附带会清除被删除会议的员工、广告、节点）
                updateMeetInfo(comId, meetArray);

                for (Meet meet : meetArray) {
                    MeetInfo meetInfo = meet.getMeetInfo();
                    meetInfo.setComId(comId);

                    long meetId = meetInfo.getId();
                    List<EntryInfo> entryArray = meet.getEntryArray();
                    updateEntryInfo(comId, meetId, entryArray);

                    List<AdvertInfo> advertArray = meet.getAdvertArray();
                    updateAdvertInfo(comId, meetId, advertArray);

                    List<FlowInfo> flowArray = meet.getFlowArray();
                    updateFlowInfo(comId, meetId, flowArray);
                }

                callback.onFinished();

                checkData();
            }
        });
    }

    /***
     * 更新会议信息
     * @param comId
     * @param meetArray
     */
    private void updateMeetInfo(int comId, List<Meet> meetArray) {
        Map<Long, MeetInfo> remoteData = new HashMap<>();
        for (Meet meet : meetArray) {
            MeetInfo meetInfo = meet.getMeetInfo();
            meetInfo.setComId(comId);
            remoteData.put(meetInfo.getId(), meetInfo);
        }

        List<MeetInfo> meetInfoList = DaoManager.get().queryMeetInfoByComId(comId);
        Iterator<MeetInfo> iterator = meetInfoList.iterator();
        while (iterator.hasNext()) {
            MeetInfo next = iterator.next();
            long meetId = next.getId();
            if (!remoteData.containsKey(meetId)) {
                List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetId);
                DaoManager.get().deleteEntryInfos(entryInfos);
                List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetId);
                DaoManager.get().deleteAdvertInfos(advertInfos);
                List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(meetId);
                DaoManager.get().deleteFlowInfos(flowInfos);
                iterator.remove();
                DaoManager.get().delete(next);
            }
        }

        for (Map.Entry<Long, MeetInfo> entry : remoteData.entrySet()) {
            MeetInfo value = entry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
    }

    /***
     * 更新员工库
     * @param comId
     * @param meetId
     * @param entryInfoList
     */
    private void updateEntryInfo(int comId, long meetId, List<EntryInfo> entryInfoList) {
        Map<String, EntryInfo> remoteData = new HashMap<>();
        for (EntryInfo entryInfo : entryInfoList) {
            entryInfo.setMeetId(meetId);
            entryInfo.setComId(comId);
            String head = entryInfo.getHead();
            String filepath = PathManager.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
            entryInfo.setHeadPath(filepath);
            remoteData.put(entryInfo.getMeetEntryId(), entryInfo);
        }

        //删除不存在的员工
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetId);
        Iterator<EntryInfo> iterator = entryInfos.iterator();
        while (iterator.hasNext()) {
            EntryInfo entryInfo = iterator.next();
            if (!remoteData.containsKey(entryInfo.getMeetEntryId())) {
                iterator.remove();
                DaoManager.get().delete(entryInfo);
            }
        }

        //添加或更新数据库
        for (Map.Entry<String, EntryInfo> entryInfoEntry : remoteData.entrySet()) {
            EntryInfo value = entryInfoEntry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
    }

    /***
     * 更新广告信息
     * @param comId
     * @param meetId
     * @param advertInfoList
     */
    private void updateAdvertInfo(int comId, long meetId, List<AdvertInfo> advertInfoList) {
        Map<Integer, AdvertInfo> remoteData = new HashMap<>();
        for (AdvertInfo advertInfo : advertInfoList) {
            advertInfo.setMeetId(meetId);
            advertInfo.setComId(comId);
            String head = advertInfo.getUrl();
            String filepath = PathManager.ADS_PATH + head.substring(head.lastIndexOf("/") + 1);
            advertInfo.setPath(filepath);
            remoteData.put(advertInfo.getAdvertId(), advertInfo);
        }

        //删除不存在的员工
        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetId);
        Iterator<AdvertInfo> iterator = advertInfos.iterator();
        while (iterator.hasNext()) {
            AdvertInfo advertInfo = iterator.next();
            if (!remoteData.containsKey(advertInfo.getAdvertId())) {
                iterator.remove();
                DaoManager.get().delete(advertInfo);
            }
        }

        for (Map.Entry<Integer, AdvertInfo> entry : remoteData.entrySet()) {
            AdvertInfo value = entry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
    }

    /***
     * 更新节点信息
     * @param comId
     * @param meetId
     * @param flowInfoList
     */
    private void updateFlowInfo(int comId, long meetId, List<FlowInfo> flowInfoList) {
        Map<String, FlowInfo> remoteData = new HashMap<>();
        for (FlowInfo flowInfo : flowInfoList) {
            flowInfo.setComId(comId);
            flowInfo.setMeetId(meetId);
            remoteData.put(flowInfo.getBegin() + flowInfo.getEnd(), flowInfo);
        }

        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(meetId);
        Iterator<FlowInfo> iterator = flowInfos.iterator();
        while (iterator.hasNext()) {
            FlowInfo flowInfo = iterator.next();
            if (!remoteData.containsKey(flowInfo.getBegin() + flowInfo.getEnd())) {
                iterator.remove();
                DaoManager.get().delete(flowInfo);
            }
        }

        for (Map.Entry<String, FlowInfo> entry : remoteData.entrySet()) {
            FlowInfo flow = entry.getValue();
            DaoManager.get().addOrUpdate(flow);
        }
    }

    private void checkData(){
        List<MeetInfo> meetInfoList = DaoManager.get().queryAll(MeetInfo.class);
        d("handleData: 222会议总数：" + meetInfoList.size());
        for (MeetInfo meetInfo : meetInfoList) {
            d("会议信息: " + meetInfo.getId() + " --- " + meetInfo.getName() + " --- " + meetInfo.getNum());
            long id = meetInfo.getId();

            List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
            for (EntryInfo entryInfo : entryInfos) {
                d( "----- 员工信息：" + entryInfo.getMeetId() + " --- " + entryInfo.getName() + " --- " + entryInfo.getMeetEntryId());
            }

            List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(id);
            for (AdvertInfo advertInfo : advertInfos) {
                d("----- 宣传信息：" + advertInfo.getMeetId() + " --- " + advertInfo.getPath());
            }

            List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(id);
            for (FlowInfo flowInfo : flowInfos) {
                d("----- 节点信息：" + flowInfo.getMeetId() + " --- " + flowInfo.getBegin() + " --- " + flowInfo.getEnd());
            }
        }
    }

    private void d(String log) {
        Log.d(TAG, log);
    }

    public interface LoadMeetCallback {
        /***
         * 开始获取会议
         */
        void onStartGetMeeting();

        /***
         * 未绑定会议室
         */
        void notBind();

        /***
         * 会议加载失败（指当前没有会议，第一次加载失败）
         * @param e
         */
        void onErrorGetMeeting(Exception e);

        /***
         * 更新会议失败（指当前有会议数据的时候，下一次加载失败）
         * @param e
         */
        void onErrorUpdateMeeting(Exception e);

        /***
         * 没有会议
         */
        void noMeeting();

        /***
         * 会议没有变化
         */
        void notChange();

        /***
         * 会议加载成功
         * @param meetingResponse
         */
        void onMeetingGeted(MeetingResponse meetingResponse);

        /***
         * 加载结束
         */
        void onFinish();
    }

    public interface HandleDataCallback{
        void onFinished();
    }
}
