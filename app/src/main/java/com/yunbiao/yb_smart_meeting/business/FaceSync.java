package com.yunbiao.yb_smart_meeting.business;

import android.util.Log;

import com.faceview.FaceManager;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.faceview.FaceSDK;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.yunbiao.yb_smart_meeting.utils.xutil.MyXutils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class FaceSync {
    private static final String TAG = "FaceSync";

    public static void downloadHead() {
        Log.e(TAG, "downloadHead: 1111111111");
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        final List<EntryInfo> entryInfos = DaoManager.get().queryEntryByComId(comId);
        Queue<EntryInfo> entryQueue = new LinkedList<>();
        entryQueue.addAll(entryInfos);

        download(entryQueue, new DownloadCallback() {
            @Override
            public void onComplete(EntryInfo entryInfo, File result) {
                entryInfo.setHeadPath(result.getPath());
                long update = DaoManager.get().update(entryInfo);
                Log.e(TAG, "onComplete: 更新数据库结果：" + update);
            }

            @Override
            public void onError(EntryInfo entryInfo, Throwable t) {

            }

            @Override
            public void onFinished() {
                updateFace(entryInfos);
            }
        });
    }

    private static void updateFace(List<EntryInfo> entryInfos) {
        for (EntryInfo entryInfo : entryInfos) {
            String meetEntryId = entryInfo.getMeetEntryId();
            String headPath = entryInfo.getHeadPath();
            //添加人脸库
            boolean b = FaceManager.getInstance().addUser(meetEntryId, headPath);
            Log.e(TAG, "updateFace: 添加人脸库：" + meetEntryId + " --- " + b);
        }
    }

    private static void download(final Queue<EntryInfo> entryQueue, final DownloadCallback callback) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            callback.onFinished();
        }

        final EntryInfo poll = entryQueue.poll();
        Log.e(TAG, "download: 开始下载：" + poll.getMeetEntryId() + " --- " + poll.getName());
        MyXutils.getInstance().downLoadFile(poll.getHead(), poll.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "onLoading: " + current + "/" + total);
            }

            @Override
            public void onSuccess(File result) {
                callback.onComplete(poll, result);
            }

            @Override
            public void onError(Throwable ex) {
                callback.onError(poll, ex);
            }

            @Override
            public void onFinished() {
                download(entryQueue, callback);
            }
        });
    }

    public interface DownloadCallback {
        void onComplete(EntryInfo entryInfo, File result);

        void onError(EntryInfo entryInfo, Throwable t);

        void onFinished();
    }

    private static void downFace(final EntryInfo entryInfo) {
        MyXutils.getInstance().downLoadFile(entryInfo.getHead(), entryInfo.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                Map<String, FaceUser> allFaceData = FaceSDK.instance().getAllFaceData();
                if (allFaceData.containsKey(entryInfo.getMeetEntryId())) {
                    FaceUser faceUser = allFaceData.get(entryInfo.getMeetEntryId());
                    faceUser.setImagePath(result.getPath());
                    FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int i) {
                            Log.e(TAG, "onUserResult: 更新结果：" + b + " --- " + i);
                        }
                    });
                } else {
                    FaceSDK.instance().addUser(entryInfo.getMeetEntryId(), result.getPath(), new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int i) {
                            Log.e(TAG, "onUserResult: 添加结果：" + b + " --- " + i);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
