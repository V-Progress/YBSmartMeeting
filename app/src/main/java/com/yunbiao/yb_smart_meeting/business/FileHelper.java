package com.yunbiao.yb_smart_meeting.business;

import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;

import java.util.ArrayList;
import java.util.List;

public class FileHelper {
    private static FileHelper instance = new FileHelper();
    private static final String TAG = "FileHelper";
    private FileDownloadQueueSet queueSet;

    public static FileHelper getInstance() {
        return instance;
    }

    private FileHelper() {
        if (queueSet == null) {
            queueSet = new FileDownloadQueueSet(listener);
            queueSet.setAutoRetryTimes(5);
            queueSet.setCallbackProgressTimes(1000);
            queueSet.setCallbackProgressMinInterval(1000);
            FileDownloader.enableAvoidDropFrame();
            FileDownloader.getImpl().setMaxNetworkThreadCount(1);
        }
    }

    private List<String> headList = new ArrayList<>();

    public void addTask(EntryInfo entryInfo){



    }

    public void addTask(final String head, String savePath) {
        Log.e(TAG, "地址：" + head);
        //如果headList不包含此地址，则添加，玩后判断是否有任务进行
        if (!headList.contains(head)) {
            headList.add(head);
            queueSet.downloadSequentially(FileDownloader.getImpl()
                    .create(head)
                    .setPath(savePath));
            queueSet.start();
        }
    }

    private FileDownloadSampleListener listener = new FileDownloadSampleListener() {
        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
            String url = task.getUrl();
            if (headList.contains(url)) {
                boolean remove = headList.remove(url);
                Log.e(TAG, "blockComplete: 删除: " + remove);
            }

            if(headList.size() <= 0){
                Log.e(TAG, "blockComplete: 已全部完成" );
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            Log.e(TAG, "error: 失败：" + task.getPath());
        }
    };
}
