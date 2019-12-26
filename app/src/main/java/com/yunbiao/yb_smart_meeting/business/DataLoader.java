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
        }, 3, 60, TimeUnit.SECONDS);
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

                        //如果错误缓存不为空并且当前错误与错误缓存一样，证明上一次也是错误，无变化
                        if (!TextUtils.isEmpty(mError) && TextUtils.equals(error, mError)) {
                            callback.notChange();
                        } else {//如果错误缓存为空，并且响应缓存也为空，代表肯定是第一次请求，所以需要提示
                            if (TextUtils.isEmpty(mResponse)) {
                                callback.onErrorGetMeeting(e);
                            } else {//响应缓存不为空，代表是请求刷新时失败，可以不处理
                                callback.onErrorUpdateMeeting(e);
                            }
                        }
                        mError = error;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d("获取成功：" + (TextUtils.isEmpty(response) ? 0 : response.length()));

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
        d("*************************************************************************");
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        List<MeetInfo> meetInfos = DaoManager.get().queryMeetByComId(comId);
        DaoManager.get().deleteMeetInfos(meetInfos);
        d("clearDataByCurrCompany: 清除会议数据：" + (meetInfos == null ? 0 : meetInfos.size()));

        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByComId(comId);
        DaoManager.get().deleteEntryInfos(entryInfos);
        d("clearDataByCurrCompany: 清除员工数据：" + (entryInfos == null ? 0 : entryInfos.size()));

        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByComdId(comId);
        DaoManager.get().deleteAdvertInfos(advertInfos);
        d("clearDataByCurrCompany: 清除广告数据：" + (advertInfos == null ? 0 : advertInfos.size()));

        List<FlowInfo> flowInfos = DaoManager.get().queryFlowByComId(comId);
        DaoManager.get().deleteFlowInfos(flowInfos);
        d("clearDataByCurrCompany: 清除节点数据：" + (flowInfos == null ? 0 : flowInfos.size()));
        d("*************************************************************************");
        d("\n");
        d("\n");
    }

    /***
     * 开始处理数据
     * @param meetingResponse
     */
    public void handleData(final MeetingResponse meetingResponse, @NonNull final HandleDataCallback callback) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
//                clearDataByCurrCompany();
                int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
                List<Meet> meetArray = meetingResponse.getMeetArray();

                //删除不存在的会议
                //更新会议信息（附带会清除被删除会议的员工、广告、节点）
                d("会议信息同步.....................................");
                updateMeetInfo(comId, meetArray);
                d("会议信息同步结束..................................");
                d("\n");
                d("\n");
                for (Meet meet : meetArray) {
                    MeetInfo meetInfo = meet.getMeetInfo();

                    d("**************************************************************************");
                    d("开始处理：" + meetInfo.getName() + " 的数据");

                    long meetId = meetInfo.getId();

                    d("\n-------------------------");
                    List<EntryInfo> entryArray = meet.getEntryArray();
                    updateEntryInfo(comId, meetId, entryArray);

                    d("\n--------------------------");
                    List<AdvertInfo> advertArray = meet.getAdvertArray();
                    updateAdvertInfo(comId, meetId, advertArray);

                    d("\n---------------------------");
                    List<FlowInfo> flowArray = meet.getFlowArray();
                    updateFlowInfo(comId, meetId, flowArray);
                    d("**************************************************************************");
                    d("\n");
                    d("\n");
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
        d("远程服务器中的会议数：" + remoteData.size());
        d("当前数据库中的会议数：" + (meetInfoList == null ? 0 : meetInfoList.size()));

        int num = 0;
        Iterator<MeetInfo> iterator = meetInfoList.iterator();
        while (iterator.hasNext()) {
            MeetInfo next = iterator.next();
            long meetId = next.getId();
            if (!remoteData.containsKey(meetId)) {
                d(".....删除：" + next.getName());
                List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetId);
                DaoManager.get().deleteEntryInfos(entryInfos);
                d("......删除员工：" + (entryInfos == null ? 0 : entryInfos.size()));

                List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetId);
                DaoManager.get().deleteAdvertInfos(advertInfos);
                d(".....删除广告：" + (advertInfos == null ? 0 : advertInfos.size()));

                List<FlowInfo> flowInfos = DaoManager.get().queryFlowByMeetId(meetId);
                DaoManager.get().deleteFlowInfos(flowInfos);
                d(".....删除节点：" + (flowInfos == null ? 0 : flowInfos.size()));

                iterator.remove();
                DaoManager.get().delete(next);
                num++;
            }
        }
        d("共删除不存在的会议：" + num);

        for (Map.Entry<Long, MeetInfo> entry : remoteData.entrySet()) {
            MeetInfo value = entry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
        d("已添加或更新会议信息数：" + remoteData.size());
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
        d("远程服务器中共有员工数：" + remoteData.size());
        d("当前数据库中共有员工数：" + (entryInfos == null ? 0 : entryInfos.size()));

        int num = 0;
        Iterator<EntryInfo> iterator = entryInfos.iterator();
        while (iterator.hasNext()) {
            EntryInfo entryInfo = iterator.next();
            if (!remoteData.containsKey(entryInfo.getMeetEntryId())) {
                iterator.remove();
                DaoManager.get().delete(entryInfo);
                num++;
            }
        }
        d("已删除不存在的员工：" + num);

        //添加或更新数据库
        for (Map.Entry<String, EntryInfo> entryInfoEntry : remoteData.entrySet()) {
            EntryInfo value = entryInfoEntry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
        d("已添加或更新员工数：" + remoteData.size());
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
        d("远程服务器中的数据：" + remoteData.size());
        d("当前数据库中的数据：" + (advertInfos == null ? 0 : advertInfos.size()));

        int num = 0;
        if (advertInfos != null) {
            Iterator<AdvertInfo> iterator = advertInfos.iterator();
            while (iterator.hasNext()) {
                AdvertInfo advertInfo = iterator.next();
                if (!remoteData.containsKey(advertInfo.getAdvertId())) {
                    iterator.remove();
                    DaoManager.get().delete(advertInfo);
                    num++;
                }
            }
        }
        d("共删除不存在的数据：" + num);

        for (Map.Entry<Integer, AdvertInfo> entry : remoteData.entrySet()) {
            AdvertInfo value = entry.getValue();
            DaoManager.get().addOrUpdate(value);
        }
        d("已添加或更新广告数：" + remoteData.size());
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
        d("远程服务器中的节点数：" + remoteData.size());
        d("当前数据库中的节点数：" + flowInfos.size());

        int num = 0;
        if (flowInfos != null) {
            Iterator<FlowInfo> iterator = flowInfos.iterator();
            while (iterator.hasNext()) {
                FlowInfo flowInfo = iterator.next();
                if (!remoteData.containsKey(flowInfo.getBegin() + flowInfo.getEnd())) {
                    iterator.remove();
                    DaoManager.get().delete(flowInfo);
                    num++;
                }
            }
        }
        d("已删除不存在的节点：" + num);

        for (Map.Entry<String, FlowInfo> entry : remoteData.entrySet()) {
            FlowInfo flow = entry.getValue();
            DaoManager.get().addOrUpdate(flow);
        }
        d("已添加或更新节点数：" + remoteData.size());
    }

    private void checkData() {
        d("================================================================================");
        List<MeetInfo> meetInfoList = DaoManager.get().queryAll(MeetInfo.class);
        d("handleData: 222会议总数：" + meetInfoList.size());
        for (MeetInfo meetInfo : meetInfoList) {
            d("会议信息: " + meetInfo.getId() + " --- " + meetInfo.getName() + " --- " + meetInfo.getNum());
            long id = meetInfo.getId();

            List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
            for (EntryInfo entryInfo : entryInfos) {
                d("----- 员工信息：" + entryInfo.getMeetId() + " --- " + entryInfo.getName() + " --- " + entryInfo.getMeetEntryId());
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
        d("================================================================================");
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

    public interface HandleDataCallback {
        void onFinished();
    }
}
