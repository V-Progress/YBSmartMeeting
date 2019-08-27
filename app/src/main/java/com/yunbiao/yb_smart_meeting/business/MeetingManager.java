package com.yunbiao.yb_smart_meeting.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.FlowInfo;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.Meet;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.bean.meet_model.MeetingResponse;
import com.yunbiao.yb_smart_meeting.faceview.FaceSDK;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.utils.logutils.LogUtils;
import com.yunbiao.yb_smart_meeting.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
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
    private Map<Long, Timer> timerMap = new HashMap();

    private MeetingManager() {

    }

    public static MeetingManager getInstance() {
        return meetingManager;
    }

    public void request(final String url, final Map<String, String> params, Consumer<MeetingResponse> consumer, Consumer<Throwable> errConsumer) {
        d("开始请求数据-------------------------");
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

                SpUtils.saveStr(SpUtils.MEETING_CACHE, string);

                try {
                    MeetingResponse meetingResponse = new Gson().fromJson(string, MeetingResponse.class);
                    return Observable.just(meetingResponse);
                } catch (Exception e) {
                    return Observable.error(new NetException());
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, errConsumer);
    }

    public void init() {
        clearTimer();
        EventBus.getDefault().post(new MeetingEvent(MeetingEvent.INIT,null));
        Map<String, String> params = new HashMap<>();
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        request(ResourceUpdate.GET_MEETING, params, new Consumer<MeetingResponse>() {
            @Override
            public void accept(MeetingResponse meetingResponse) throws Exception {
                if (meetingResponse.getStatus() == 2) {
                    DaoManager.get().deleteAll(MeetInfo.class);
                    DaoManager.get().deleteAll(EntryInfo.class);
                    DaoManager.get().deleteAll(AdvertInfo.class);
                    DaoManager.get().deleteAll(FlowInfo.class);
                    SpUtils.saveStr(SpUtils.MEETING_CACHE, "");
                    EventBus.getDefault().post(new MeetingEvent(MeetingEvent.NO_MEETING, null));
                    return;
                }

                //删除不存在的会议
                deleteNoExist(meetingResponse);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                d("请求失败：" + throwable.getClass().getSimpleName());
                String str = SpUtils.getStr(SpUtils.MEETING_CACHE);
                if (TextUtils.isEmpty(str)) {
                    return;
                } else {
                    MeetingResponse meetingResponse = new Gson().fromJson(str, MeetingResponse.class);
                    deleteNoExist(meetingResponse);
                }
            }
        });
    }

    private void clearTimer() {
        d("结束所有定时任务-------------------------");
        for (Map.Entry<Long, Timer> longTimerEntry : timerMap.entrySet()) {
            longTimerEntry.getValue().cancel();
            d("结束：" + longTimerEntry.getKey());
        }
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

        //处理数据
        saveMeet(meetingResponse);
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
                            d(flowInfo.toString());
                            flowInfo.setMeetId(meetId);
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

                checkEntryHead();
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    class DownloadBean {
        private long id;
        private Queue<EntryInfo> entryQueue;

        public DownloadBean(long id, Queue<EntryInfo> entryQueue) {
            this.id = id;
            this.entryQueue = entryQueue;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Queue<EntryInfo> getEntryQueue() {
            return entryQueue;
        }

        public void setEntryQueue(Queue<EntryInfo> entryQueue) {
            this.entryQueue = entryQueue;
        }
    }

    private void checkEntryHead() {
        d("开始检测人脸头像--------------------------");
        final List<MeetInfo> meetInfos = DaoManager.get().queryAll(MeetInfo.class);

        Queue<DownloadBean> downloadQueue = new LinkedList<>();
        for (MeetInfo meetInfo : meetInfos) {
            long id = meetInfo.getId();
            List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(id);
            Queue<EntryInfo> queue = new LinkedList<>();
            for (EntryInfo entryInfo : entryInfos) {//循环判断头像是否存在，如果不存在就加入队列
                String headPath = entryInfo.getHeadPath();
                String head = entryInfo.getHead();
                if (TextUtils.isEmpty(headPath)) {
                    if (TextUtils.isEmpty(head)) {
                        continue;
                    } else {
                        headPath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                        entryInfo.setHeadPath(headPath);
                        DaoManager.get().addOrUpdate(entryInfo);
                    }
                }

                if (new File(headPath).exists()) {
                    d("已存在");
                    continue;
                }

                queue.add(entryInfo);
            }

            //如果队列有数据再添加进map
            if (queue.size() > 0) {
                d("添加：" + id + "，未下载：" + queue.size());
                downloadQueue.add(new DownloadBean(id, queue));
            }
        }

        startDownload(downloadQueue, new Runnable() {
            @Override
            public void run() {
                d("已全部下载完成");
                handleEntry();
                handleMeetProcess();
            }
        });
    }

    private void handleEntry(){
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Map<String, FaceUser> allFaceData = FaceSDK.instance().getAllFaceData();

                List<EntryInfo> entryInfos = DaoManager.get().queryAll(EntryInfo.class);
                for (EntryInfo entryInfo : entryInfos) {
                    if(allFaceData.containsKey(entryInfo.getId())){
                        FaceUser faceUser = allFaceData.get(entryInfo.getId());
                        String imagePath = faceUser.getImagePath();
                        if(!TextUtils.equals(imagePath,entryInfo.getHeadPath())){
                            faceUser.setImagePath(entryInfo.getHeadPath());
                            FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                                @Override
                                public void onUserResult(boolean b, int i) {
                                    Log.e(TAG, "onUserResult: 更新：" + b + " --- "+i );
                                }
                            });
                        }
                    } else {
                        FaceSDK.instance().addUser(String.valueOf(entryInfo.getId()), entryInfo.getHeadPath(), new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                Log.e(TAG, "onUserResult: 添加：" + b + " --- "+i );
                            }
                        });
                    }
                }

            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    //检查当前的会议
    private void handleMeetProcess() {
        d("开始处理定时任务-------------------------");
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                List<MeetInfo> meetInfos = DaoManager.get().queryAll(MeetInfo.class);
                if (meetInfos == null || meetInfos.size() <= 0) {
                    d("没有会议，执行结束");
                }

                Date currDate = null;
                MeetInfo tempMeetInfo = null;
                boolean haveMeeting = false;
                for (final MeetInfo meetInfo : meetInfos) {
                    Date begin = formater.parse(meetInfo.getBeginTime());
                    Date end = formater.parse(meetInfo.getEndTime());
                    long id = meetInfo.getId();

                    Date date = new Date();
                    if(date.after(begin) && date.before(end)){
                        haveMeeting = true;
                    } else {
                        //如果为null就先赋值
                        if (currDate == null) {
                            currDate = begin;
                            tempMeetInfo = meetInfo;
                        } else if(currDate.after(begin)){
                            currDate = begin;
                            tempMeetInfo = meetInfo;
                        }
                    }

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.BEGINED, meetInfo));
                        }
                    }, begin);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new MeetingEvent(MeetingEvent.END, meetInfo));
                        }
                    }, end);
                    timerMap.put(id, timer);
                }

                //发送预加载事件
                if (tempMeetInfo != null && !haveMeeting) {
                    EventBus.getDefault().post(new MeetingEvent(MeetingEvent.PRELOADING, tempMeetInfo));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    private void startDownload(final Queue<DownloadBean> downloadBeans, final Runnable runnable) {
        if (downloadBeans == null || downloadBeans.size() <= 0) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        DownloadBean downloadBean = downloadBeans.poll();
        final long id = downloadBean.id;
        Queue<EntryInfo> entryQueue = downloadBean.entryQueue;
        d("开始下载：" + id);
        download(entryQueue, new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: ----- " + id + "已全部下载完成");
                startDownload(downloadBeans, runnable);
            }
        });
    }

    private void download(final Queue<EntryInfo> entryQueue, final Runnable runnable) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        EntryInfo poll = entryQueue.poll();

        String head = poll.getHead();
        String headPath = poll.getHeadPath();

        d("准备下载：" + poll.getName() + " --- " + head + " --- " + headPath);

        MyXutils.getInstance().downLoadFile(head, headPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                d("下载完成：" + result.getPath());
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinished() {
                download(entryQueue, runnable);
            }
        });
    }

    class NetException extends Exception {

    }

    private void d(String log) {
        Log.d(TAG, log);
    }
}
