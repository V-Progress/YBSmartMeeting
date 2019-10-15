package com.yunbiao.yb_smart_meeting.business;

import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Request;

public class MeetingLoader {
    private static final String TAG = "MeetingLoader";
    private ScheduledExecutorService executor;

    private MeetingLoader() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    private boolean isAutoThreadRunning = false;

    public static MeetingLoader meetingLoader = new MeetingLoader();

    public static MeetingLoader i() {
        return meetingLoader;
    }

    public interface LoadListener {
        void onStart();

        void onError();

        void onSuccess();

        void noMeeting();

        void onFinish();

        void onPreload(MeetInfo currentMeetInfo);

        void onBegan(MeetInfo currentMeetInfo);

        void onEnded(MeetInfo currentMeetInfo);

        void onNextMeet(MeetInfo nextMeetInfo);
    }

    public void startAutoGetMeeting(final LoadListener loadListener) {
        if (isAutoThreadRunning) {
            return;
        }
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                isAutoThreadRunning = true;
                Log.e(TAG, "run: 开始自动获取会议");
                getAllMeeting(loadListener);
            }
        }, 3, 60, TimeUnit.SECONDS);
    }

    public void getAllMeeting(final LoadListener loadListener) {
        d("开始请求数据-------------------------");
        OkHttpUtils.post()
                .url(ResourceUpdate.GET_MEETING)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        if (loadListener != null) {
                            loadListener.onStart();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("请求失败：" + (e == null ? "null" : e.getMessage()));
                        if (loadListener != null) {
                            loadListener.onError();
                            loadListener.onFinish();
                        }
                    }


                    @Override
                    public void onResponse(String response, int id) {
                        d("请求成功：" + response);
//                        String cacheMeeting = SpUtils.getStr(SpUtils.MEETING_CACHE);
//                        if(!isInit){
//                            if(TextUtils.equals(cacheMeeting,response)){
//                                d("会议无变化，不继续请求");
//                                if(loadListener != null){
//                                    loadListener.onFinish();
//                                }
//                                return;
//                            }
//                        }
//                        isInit = false;

                        SpUtils.saveStr(SpUtils.MEETING_CACHE, response);

                        MeetingResponse meetingResponse = new Gson().fromJson(response, MeetingResponse.class);
                        if (meetingResponse == null) {
                            if (loadListener != null) {
                                loadListener.onError();
                                loadListener.onFinish();
                            }
                            return;
                        }

                        //清除数据库
                        clearDB();

                        if (meetingResponse.getStatus() == 2) {
                            if (loadListener != null) {
                                loadListener.noMeeting();
                                loadListener.onFinish();
                            }
                            return;
                        }

                        //删除不存在的会议
                        deleteNoExist(meetingResponse);

                        //保存会议
                        saveMeet(meetingResponse, loadListener);
                    }
                });
    }

    //清除数据库
    public void clearDB() {
        DaoManager.get().deleteAll(MeetInfo.class);
        DaoManager.get().deleteAll(EntryInfo.class);
        DaoManager.get().deleteAll(FlowInfo.class);
        DaoManager.get().deleteAll(AdvertInfo.class);
    }

    //删除不存在的会议
    public void deleteNoExist(MeetingResponse meetingResponse) {
        d("删除不存在的会议-------------------------");
        List<Long> remoteIdList = new ArrayList<>();
        List<Meet> array = meetingResponse.getMeetArray();
        for (Meet meet : array) {
            MeetInfo meetInfo = meet.getMeetInfo();
            long id = meetInfo.getId();
            remoteIdList.add(id);
        }

        List<MeetInfo> meetInfos = DaoManager.get().queryAll(MeetInfo.class);
        for (MeetInfo meetInfo : meetInfos) {
            long id = meetInfo.getId();
            if (!remoteIdList.contains(id)) {
                long delete = DaoManager.get().delete(meetInfo);
                d("远程库中不包含：" + id + "，删除：" + delete);
            }
        }
    }

    private void d(String log) {
        Log.d(TAG, log);
    }

    //保存会议
    public void saveMeet(final MeetingResponse meetingResponse, final LoadListener loadListener) {
        d("开始保存会议信息-------------------------");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                List<Meet> meetArray = meetingResponse.getMeetArray();
                d("共有会议：" + meetArray.size());

                for (Meet meet : meetArray) {
                    d(meet.toString());

                    MeetInfo meetInfo = meet.getMeetInfo();
                    long meetId = meetInfo.getId();//广告应对应这一个id

                    //先存会议信息
                    long add = DaoManager.get().addOrUpdate(meetInfo);
                    d("saveMeet: 保存会议 " + add);

                    //存员工数据
                    List<EntryInfo> entryInfoArray = meet.getEntryArray();
                    if (entryInfoArray != null && entryInfoArray.size() > 0) {
                        for (EntryInfo entryInfo : entryInfoArray) {
                            entryInfo.setId(entryInfo.getMeetEntryId());
                            entryInfo.setMeetId(meetId);
                            String head = entryInfo.getHead();
                            String filepath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                            entryInfo.setHeadPath(filepath);
                            d(entryInfo.toString());
                            long add1 = DaoManager.get().addOrUpdate(entryInfo);
                            d("saveMeet: 添加参会人 " + add1);
                        }
                    } else {
                        d("没有人员数据");
                    }

                    //存节点数据
                    List<FlowInfo> flowInfoArray = meet.getFlowArray();
                    if (flowInfoArray != null && flowInfoArray.size() > 0) {
                        for (FlowInfo flowInfo : flowInfoArray) {
                            flowInfo.setMeetId(meetId);
                            d(flowInfo.toString());
                            long add1 = DaoManager.get().addOrUpdate(flowInfo);
                            d("saveMeet: 添加flow " + add1);
                        }
                    } else {
                        d("没有节点数据");
                    }

                    //存宣传数据
                    List<AdvertInfo> advertArray = meet.getAdvertArray();
                    if (advertArray != null && advertArray.size() > 0) {
                        for (AdvertInfo advertInfo : advertArray) {
                            advertInfo.setMeetId(meetId);
                            advertInfo.setId(advertInfo.getAdvertId());
                            String url = advertInfo.getUrl();
                            String filepath = Constants.ADS_PATH + url.substring(url.lastIndexOf("/") + 1);
                            advertInfo.setPath(filepath);
                            d(advertInfo.toString());
                            long add1 = DaoManager.get().addOrUpdate(advertInfo);
                            d("添加宣传：" + add1);
                        }
                    } else {
                        d("没有宣传数据");
                    }
                }

                if (loadListener != null) {
                    loadListener.onSuccess();
                    loadListener.onFinish();
                }
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    private DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Timer timer = null;
    private int currMeetNum = 1;

    /***
     * 加载当前会议或最近的会议
     */
    public void loadCurrentMeeting(final LoadListener loadListener) {
//        MeetInfo currentMeetInfo = meetingProcess.getCurrentMeetInfo();
//        Date beginDate = meetingProcess.getBeginDate();
//        Date endDate = meetingProcess.getEndDate();

        //判断如果当前有正在进行的会议则不刷新，等会议结束后再刷新
//        if (currentMeetInfo != null && beginDate != null && endDate != null) {
//            Date date = new Date();
//            if (date.after(beginDate) && date.before(endDate)) {
//                if (loadListener != null) {
//                    loadListener.onBegan(meetingProcess.getCurrentMeetInfo());
//                }
//                return;
//            }
//        }

        final MeetInfo meetInfo = DaoManager.get().queryByMeetNum(currMeetNum);
        //没有num为1的会议时表示没有会议
        if (currMeetNum == 1 && meetInfo == null) {
            if (loadListener != null) {
                loadListener.noMeeting();
            }
            return;
        }

        if(meetInfo == null){
            return;
        }

        String beginTime = meetInfo.getBeginTime();
        String endTime = meetInfo.getEndTime();

        try {
            Date currDate = new Date();
            Date begin = formater.parse(beginTime);
            Date end = formater.parse(endTime);

            //开始之前
            if (currDate.before(begin)) {
                if(loadListener != null){
                    loadListener.onPreload(meetInfo);
                }
            } else if (currDate.after(end)) {//已经结束
                currMeetNum++;
                loadCurrentMeeting(loadListener);
                return;
            }

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (loadListener != null) {
                        loadListener.onBegan(meetInfo);
                    }
                }
            }, begin);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (loadListener != null) {
                        loadListener.onEnded(meetInfo);
                    }
                }
            }, end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void loadNext(int num,LoadListener loadListener){
        MeetInfo nextMeetInfo = DaoManager.get().queryByMeetNum(num+1);
        if(loadListener != null){
            loadListener.onNextMeet(nextMeetInfo);
        }
    }
}
