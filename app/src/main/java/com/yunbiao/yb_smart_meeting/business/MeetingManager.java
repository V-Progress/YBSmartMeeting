package com.yunbiao.yb_smart_meeting.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.Meet;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.logutils.LogUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

public class MeetingManager {
    private static final String TAG = "MeetingManager";
    private static MeetingManager meetingManager = new MeetingManager();

    private DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private MeetingManager() {

    }

    public static MeetingManager getInstance() {
        return meetingManager;
    }

    public void init() {
        Map<String, String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        request(ResourceUpdate.GET_MEETING, params, new Consumer<MeetingResponse>() {
            @Override
            public void accept(MeetingResponse meetingResponse) throws Exception {

                //删除不存在的会议
                deleteNoExist(meetingResponse);
            }
        });
    }

    public void request(final String url, final Map<String, String> params,Consumer<MeetingResponse> consumer) {
        Observable.defer(new Callable<ObservableSource<MeetingResponse>>() {
            @Override
            public ObservableSource<MeetingResponse> call() throws Exception {
                d("获取会议数据");
                d("地址：" + url);
                d("参数：" + params.toString());
                Response response = OkHttpUtils.post().url(url).params(params).build().execute();
                if (response == null) {
                    return Observable.error(new NetException());
                }
                String string = response.body().string();
                if (TextUtils.isEmpty(string)) {
                    return Observable.error(new NetException());
                }
                d(string);
                LogUtils.print(string);

                try {
                    MeetingResponse meetingResponse = new Gson().fromJson(string, MeetingResponse.class);
                    return Observable.just(meetingResponse);
                } catch (Exception e) {
                    return Observable.error(new NetException());
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        d("请求失败：" + throwable.getClass().getSimpleName());
                    }
                });
    }

    //删除不存在的会议
    public void deleteNoExist(MeetingResponse meetingResponse){
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
            if(!remoteIdList.contains(id)){
                long delete = DaoManager.get().delete(meetInfo);
                d("远程库中不包含：" + id + "，删除：" + delete);
            }
        }

        //处理数据
        saveMeet(meetingResponse);
    }

    //保存会议
    public void saveMeet(final MeetingResponse meetingResponse){
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                d("开始处理数据");
                List<Meet> meetArray = meetingResponse.getMeetArray();
                for (Meet meet : meetArray) {
                    d(meet.toString());

                    MeetInfo meetInfo = meet.getMeetInfo();
                    long meetId = meetInfo.getId();//广告应对应这一个id

                    long add = DaoManager.get().addOrUpdate(meetInfo);
                    d( "saveMeet: 保存会议 " + add);

                    List<EntryInfo> entryInfoArray = meet.getEntryInfoArray();
                    if(entryInfoArray != null && entryInfoArray.size() > 0){
                        for (EntryInfo entryInfo : entryInfoArray) {
                            d(entryInfo.toString());
                            entryInfo.setMeetId(meetId);
                            String head = entryInfo.getHead();
                            String filepath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                            entryInfo.setHeadPath(filepath);
                            long add1 = DaoManager.get().addOrUpdate(entryInfo);
                            d( "saveMeet: 添加参会人 " + add1);
                        }
                    } else {
                        d("没有人员数据");
                    }

                    List<FlowInfo> flowInfoArray = meet.getFlowInfoArray();
                    if(flowInfoArray != null && flowInfoArray.size() > 0){
                        for (FlowInfo flowInfo : flowInfoArray) {
                            d(flowInfo.toString());
                            flowInfo.setMeetId(meetId);
                            long add1 = DaoManager.get().addOrUpdate(flowInfo);
                            d("saveMeet: 添加flow " + add1);
                        }
                    } else {
                        d("没有节点数据");
                    }

                    List<AdvertInfo> advertArray = meet.getAdvertArray();
                    if(advertArray != null && advertArray.size() > 0){
                        for (AdvertInfo advertInfo : advertArray) {
                            d(advertInfo.toString());
                            advertInfo.setMeetId(meetId);
                            String url = advertInfo.getUrl();
                            String filepath = Constants.ADS_PATH + url.substring(url.lastIndexOf("/") + 1);
                            advertInfo.setPath(filepath);
                             long add1 = DaoManager.get().addOrUpdate(advertInfo);
                            d("添加宣传：" + add1);
                        }
                    } else {
                        d("没有宣传数据");
                    }
                }

                checkCurrMeet();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    //检查当前的会议
    private void checkCurrMeet(){
        d("开始检测");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                List<MeetInfo> meetInfos = DaoManager.get().queryAll(MeetInfo.class);
                d("共有会议信息：" + meetInfos.size());

                Date currDate = new Date();
                Date endDate = new Date();
                long mId = 0;
                for (MeetInfo meetInfo : meetInfos) {
                    Date begin = formater.parse(meetInfo.getBeginTime());
                    endDate = formater.parse(meetInfo.getEndTime());
                    if (currDate.after(begin) && currDate.before(endDate)) {
                        mId = meetInfo.getId();
                        currDate = begin;
                        break;
                    } else if(currDate.before(begin)){
                        mId = meetInfo.getId();
                        currDate = begin;
                    }
                }
                d(formater.format(currDate) + " --- " + formater.format(endDate) + " --- " + mId);



            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    class NetException extends Exception {

    }

    private void d(String log){
        Log.d(TAG, log);
    }
}
