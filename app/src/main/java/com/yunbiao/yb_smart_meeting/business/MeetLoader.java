package com.yunbiao.yb_smart_meeting.business;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.faceview.FaceManager;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.utils.xutil.MyXutils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class MeetLoader {
    private static final String TAG = "MeetLoader";
    private static MeetLoader meetLoader = new MeetLoader();
    private static int NUM_CURR_MEET = 1;
    private static int NUM_NEXT_MEET = 2;

    public static MeetLoader getInstance() {
        return meetLoader;
    }

    public void load(@NonNull MeetCallback meetCallback) {
        MeetInfo currMeetInfo = DaoManager.get().queryMeetInfoByNum(NUM_CURR_MEET);
        Log.e(TAG, "load: 当前会议：" + (currMeetInfo == null ? "没有会议" : (currMeetInfo.getName() + " --- 时间：" + currMeetInfo.getBeginTime() + " --- " + currMeetInfo.getEndTime())));
        meetCallback.onLoadCurrentMeet(currMeetInfo);

        MeetInfo nextMeetInfo = DaoManager.get().queryMeetInfoByNum(NUM_NEXT_MEET);
        Log.e(TAG, "load: 下个会议：" + (nextMeetInfo == null ? "没有会议" : (nextMeetInfo.getName() + " --- 时间：" + nextMeetInfo.getBeginTime() + " --- " + nextMeetInfo.getEndTime())));
        meetCallback.onLoadNextMeet(nextMeetInfo);
    }

    public interface MeetCallback {
        void onLoadCurrentMeet(MeetInfo meetInfo);

        void onLoadNextMeet(MeetInfo meetInfo);
    }

    public void loadFaceData(long meetId) {
        List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetId);
        Log.e(TAG, "loadFaceData11111: " + (entryInfos == null ? 0 :entryInfos.size()));

        Queue<EntryInfo> entryQueue = new LinkedList<>();
        for (EntryInfo entryInfo : entryInfos) {
            entryQueue.add(entryInfo);
        }
        Log.e(TAG, "loadFaceData22222: " + (entryQueue == null ? 0 :entryQueue.size()));

        FaceManager.getInstance().clearCache();

        addToFace(entryQueue, new AddFaceCallback() {
            @Override
            public void onSingleComplete(EntryInfo entryInfo, File file) {
                boolean b = FaceManager.getInstance().addUser(entryInfo.getMeetEntryId(), file.getPath());
                Log.e(TAG, "onSingleComplete: 添加结果：" + entryInfo.getMeetEntryId() + " --- " + b);
            }

            @Override
            public void onSingleFailed(EntryInfo entryInfo, Throwable t) {
                Log.e(TAG, "onSingleFailed: 下载失败：" + entryInfo.getMeetEntryId() + " --- " + (t == null ? "NULL" : t.getMessage()));
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "onFinished: 添加结束");
            }
        });
    }

    public void addToFace(final Queue<EntryInfo> entryQueue, final AddFaceCallback callback) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            callback.onFinished();
            return;
        }

        final EntryInfo entryInfo = entryQueue.poll();
        String headPath = entryInfo.getHeadPath();
        Log.e(TAG, "addToFace: " + entryInfo.getName() + " --- " + headPath + " --- " + entryInfo.getHead());

        final File file = new File(headPath);
        //如果文件存在
        if(file != null && file.exists()){
            //检查是否添加入库
            callback.onSingleComplete(entryInfo,file);
            addToFace(entryQueue, callback);
            return;
        }
        MyXutils.getInstance().downLoadFile(entryInfo.getHead(), entryInfo.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "onLoading: " + current + " --- " + total);
            }

            @Override
            public void onSuccess(File result) {
                callback.onSingleComplete(entryInfo,file);
            }

            @Override
            public void onError(Throwable ex) {
                entryQueue.offer(entryInfo);
                callback.onSingleFailed(entryInfo,ex);
            }

            @Override
            public void onFinished() {
                addToFace(entryQueue, callback);
            }
        });
    }

    public interface AddFaceCallback {
        void onSingleComplete(EntryInfo entryInfo, File file);

        void onSingleFailed(EntryInfo entryInfo, Throwable t);

        void onFinished();
    }
}
