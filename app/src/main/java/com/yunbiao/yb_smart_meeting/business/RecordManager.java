package com.yunbiao.yb_smart_meeting.business;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_meeting.APP;
import com.yunbiao.yb_smart_meeting.Config;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.EntryInfo;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.db2.RecordInfo;
import com.yunbiao.yb_smart_meeting.system.HeartBeatClient;
import com.yunbiao.yb_smart_meeting.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class RecordManager {
    private static final String TAG = "RecordManager";
    private static RecordManager recordManager = new RecordManager();
    private final ScheduledExecutorService executor;

    public static RecordManager get() {
        return recordManager;
    }

    private RecordManager() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(autoSendRunnable, 10, 30, TimeUnit.MINUTES);
        executor.execute(initRunnable);
    }

    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            clearJDVerifyRecord();
        }
    };

    /*定时清除京东SDK验证记录*/
    private void clearJDVerifyRecord() {
        int count = 0;
        int failed = 0;
        File dirFile = new File(APP.getContext().getDir("VerifyRecord", Context.MODE_PRIVATE).getAbsolutePath());
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file != null) {
                if (file.delete()) {
                    count++;
                } else {
                    failed++;
                }
            } else {
                failed++;
            }
        }
        Log.e(TAG, "总共清除记录：" + count + "条" + "，失败：" + failed + "条");
    }

    private Runnable autoSendRunnable = new Runnable() {
        @Override
        public void run() {
            List<RecordInfo> recordInfos = DaoManager.get().queryRecordByUpload(false);
            if (recordInfos == null || recordInfos.size() <= 0) {
                d("没有记录...");
                return;
            }
        }
    };

    private void d(String log) {
        Log.d(TAG, log);
    }

    private final int TAG_MAX_TIME = 5;
    private int verifyTag = 0;

    public void checkPassage(VerifyResult verifyResult) {
        FaceUser user = verifyResult.getUser();
        if (user == null || TextUtils.isEmpty(user.getUserId())) {
            return;
        }

        byte[] faceImageBytes = verifyResult.getFaceImageBytes();
        String userId = user.getUserId();
        long id = Long.parseLong(userId);

        //生成签到时间
        final long currTime = System.currentTimeMillis();
        final RecordInfo recordInfo = new RecordInfo();
        recordInfo.setTime(currTime);

        if (!canPass(recordInfo)) {
            return;
        }

        if (currMeetingEntryMap.size() <= 0) {
            d("没有会议");
            return;
        }

        if(!currMeetingEntryMap.containsKey(id)){
            d("查无此人");
            return;
        }

        EntryInfo entryInfo = currMeetingEntryMap.get(id);
        recordInfo.setMeetId(entryInfo.getMeetId());
        recordInfo.setMeetEntryId(entryInfo.getMeetEntryId());
        recordInfo.setName(entryInfo.getName());
        recordInfo.setType(entryInfo.getType());
        int similar = (int) (verifyResult.getVerifyScore() * 100);
        recordInfo.setSmilar(similar);

        File imgFile = saveBitmap(recordInfo.getTime(), faceImageBytes);
        recordInfo.setHeadPath(imgFile.getPath());
        recordInfo.setUpload(false);

        Log.e(TAG, "checkPassage: ----  可以通过");
        EventBus.getDefault().post(recordInfo);

        sendRecord(recordInfo);
    }

    private void sendRecord(final RecordInfo recordInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("comId", SpUtils.getInt(SpUtils.COMPANY_ID) + "");
        params.put("meetId", recordInfo.getMeetId() + "");
        params.put("meetEntryId", recordInfo.getMeetEntryId() + "");
        params.put("deviceNo", HeartBeatClient.getDeviceNo());
        params.put("type", recordInfo.getType() + "");
        params.put("isPass", "0");
        params.put("similar", recordInfo.getSmilar() + "");

        File file = new File(recordInfo.getHeadPath());

        OkHttpUtils.post()
                .url(ResourceUpdate.SEND_RECORD)
                .params(params)
                .addFile("heads", file.getName(), file)
                .build()
                .execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                recordInfo.setUpload(false);
                long add = DaoManager.get().add(recordInfo);
                Log.e(TAG, "onResponse: 添加记录： " + add );
            }

            @Override
            public void onResponse(String response, int id) {
                recordInfo.setUpload(true);
                long add = DaoManager.get().add(recordInfo);
                Log.e(TAG, "onResponse: 添加记录： " + add );
            }
        });
    }

    private long verifyOffsetTime = 2000;//验证间隔时间
    private Map<Long, Long> passageMap = new HashMap<>();

    private boolean canPass(RecordInfo recordInfo) {
        long entryId = recordInfo.getMeetEntryId();
        if (!passageMap.containsKey(entryId)) {
            passageMap.put(entryId, recordInfo.getTime());
            return true;
        }

        long lastTime = passageMap.get(entryId);
        long currTime = recordInfo.getTime();
        boolean isCanPass = (currTime - lastTime) > verifyOffsetTime;
        if (isCanPass) {
            passageMap.put(entryId, currTime);
        }
        return isCanPass;
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        long start = System.currentTimeMillis();
        File filePic;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.RECORD_PATH + "/" + today + "/" + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            image.compress(Bitmap.CompressFormat.JPEG, Config.getCompressRatio(), fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        long end = System.currentTimeMillis();
        Log.e("Compress", "saveBitmap: 压缩耗时----- " + (end - start));
        return filePic;
    }

    private Map<Long,EntryInfo> currMeetingEntryMap = new HashMap<>();
    public void setEntryList(List<EntryInfo> entryInfos) {
        currMeetingEntryMap.clear();
        if(entryInfos == null){
            return;
        }
        for (EntryInfo entryInfo : entryInfos) {
            currMeetingEntryMap.put(entryInfo.getId(),entryInfo);
        }
    }
}
