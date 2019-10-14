package com.yunbiao.yb_smart_meeting.business;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.utils.xutil.MyXutils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Downloader {
    private static final String TAG = "Downloader";
    private static void d(String log){
        Log.d(TAG, log);
    }

    public interface DownloadListener{
        void complete(List<EntryInfo> entryInfos);
    }

    public static void downloadHead(MeetInfo meetInfo, final DownloadListener downloadListener){
        final List<EntryInfo> entryInfos = DaoManager.get().queryEntryInfoByMeetId(meetInfo.getId());
        Queue<EntryInfo> queue = new LinkedList<>();
        for (EntryInfo entryInfo : entryInfos) {
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
        if (queue.size() <= 0) {
            downloadListener.complete(entryInfos);
            return;
        }

        downloadHead(queue, new Runnable() {
            @Override
            public void run() {
                d("已全部下载完成");
                downloadListener.complete(entryInfos);
            }
        });
    }

    private static void downloadHead(final Queue<EntryInfo> entryQueue, final Runnable runnable) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        final EntryInfo poll = entryQueue.poll();
        String head = poll.getHead();
        String headPath = poll.getHeadPath();
        d("准备下载：" + poll.getName() + " --- " + head + " --- " + headPath);
        MyXutils.getInstance().downLoadFile(head, headPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) { d("进度：" + current + " / " + total);}

            @Override
            public void onSuccess(File result) {
                d("下载完成：" + result.getPath());
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败：" + (ex == null?"null":ex.getMessage()));
            }

            @Override
            public void onFinished() {
                downloadHead(entryQueue, runnable);
            }
        });
    }

    public static void checkResource(List<AdvertInfo> advertInfos, Runnable runnable) {
        Queue<AdvertInfo> advertQueue = new ArrayDeque<>();
        Iterator<AdvertInfo> iterator = advertInfos.iterator();
        while (iterator.hasNext()) {
            AdvertInfo next = iterator.next();
            String path = next.getPath();
            String url = next.getUrl();
            if (TextUtils.isEmpty(path)) {
                if (TextUtils.isEmpty(url)) {
                    continue;
                } else {
                    path = Constants.HEAD_PATH + url.substring(url.lastIndexOf("/") + 1);
                    next.setPath(path);
                    DaoManager.get().addOrUpdate(next);
                }
            }

            if (new File(path).exists()) {
                Log.e(TAG, "已存在");
                continue;
            }
            advertQueue.offer(next);
        }

        if (advertInfos.size() <= 0) {
            runnable.run();
            return;
        }

        downloadAdvert(advertQueue, runnable);
    }

    private static void downloadAdvert(final Queue<AdvertInfo> entryQueue, final Runnable runnable) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        AdvertInfo advertInfo = entryQueue.poll();

        String head = advertInfo.getUrl();
        String headPath = advertInfo.getPath();

        Log.e(TAG, "准备下载： --- " + head + " --- " + headPath);

        MyXutils.getInstance().downLoadFile(head, headPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载完成：" + result.getPath());
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinished() {
                downloadAdvert(entryQueue, runnable);
            }
        });
    }

}
