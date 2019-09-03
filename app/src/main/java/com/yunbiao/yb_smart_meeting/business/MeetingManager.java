package com.yunbiao.yb_smart_meeting.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.activity.Event.GetMeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.bean.CompanyBean;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.Meet;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

public class MeetingManager {
    private static final String TAG = "MeetingManager";
    private static MeetingManager meetingManager = new MeetingManager();

    private MeetingManager() {

    }

    public static MeetingManager getInstance() {
        return meetingManager;
    }

    public void init() {
        loadCompany();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getAllMeeting();
            }
        },5,5, TimeUnit.MINUTES);
    }

    private void loadCompany() {
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: ----- " + response);
                if (TextUtils.isEmpty(response)) {
                    return;
                }
                CompanyBean companyBean = new Gson().fromJson(response, CompanyBean.class);

                int comid = companyBean.getCompany().getComid();
                String name = companyBean.getCompany().getComname();
                String pwd = companyBean.getCompany().getDevicePwd();
                String logoUrl = companyBean.getCompany().getComlogo();

                SpUtils.saveInt(SpUtils.COMPANY_ID, comid);
                SpUtils.saveStr(SpUtils.COMPANY_NAME, name);
                SpUtils.saveStr(SpUtils.MENU_PWD, pwd);
                SpUtils.saveStr(SpUtils.COMPANY_LOGO, logoUrl);

                //发送更新事件
                EventBus.getDefault().postSticky(new SysInfoUpdateEvent());

                //获取全部的会议
                getAllMeeting();
            }
        });
    }

    public void getAllMeeting() {
        d("开始请求数据-------------------------");
        OkHttpUtils.post()
                .url(ResourceUpdate.GET_MEETING)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("请求失败：" + (e== null?"null":e.getMessage()));
                        EventBus.getDefault().post(new GetMeetingEvent(GetMeetingEvent.GET_MEETING_FAILED));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        d("请求成功：" + response);
                        SpUtils.saveStr(SpUtils.MEETING_CACHE, response);
                        MeetingResponse meetingResponse = new Gson().fromJson(response, MeetingResponse.class);
                        if (meetingResponse == null) {
                            EventBus.getDefault().post(new GetMeetingEvent(GetMeetingEvent.GET_MEETING_FAILED));
                            return;
                        }
                        if (meetingResponse.getStatus() == 2) {
                            EventBus.getDefault().post(new GetMeetingEvent(GetMeetingEvent.NO_MEETING));
                            return;
                        }

                        //清除数据库
                        clearDB();

                        //删除不存在的会议
                        deleteNoExist(meetingResponse);

                        //处理数据
                        saveMeet(meetingResponse);
                    }
                });
    }

    //清除数据库
    public void clearDB(){
        DaoManager.get().deleteAllMeeting();
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

    //保存会议
    public void saveMeet(final MeetingResponse meetingResponse) {
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

                EventBus.getDefault().post(new GetMeetingEvent(GetMeetingEvent.COMPLETE));
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    private void d(String log) {
        Log.d(TAG, log);
    }
}
