package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.afinel.Constants;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;
import com.yunbiao.yb_smart_meeting.utils.xutil.MyXutils;
import com.yunbiao.yb_smart_meeting.views.VerticalViewPager;
import com.yunbiao.yb_smart_meeting.views.VerticalViewPagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class IntroFragment extends Fragment {
    private static final String TAG = "IntroFragment";
    private TextView tvMeetingName;
    private MeetInfo meetInfo;
    private View avLoading;
    private VerticalViewPager verticalViewPager;
    private VerticalViewPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View rootView = View.inflate(container.getContext(), R.layout.fragment_intro,null);
        tvMeetingName = rootView.findViewById(R.id.tv_meeting_name);
        avLoading = rootView.findViewById(R.id.av_loading);
        verticalViewPager = rootView.findViewById(R.id.vvp);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }


    private void initView() {
        verticalViewPager.setVertical(true);
        //设置viewpager 缓存数，可以根据需要调整
        verticalViewPager.setOffscreenPageLimit(10);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event) {
        Log.e(TAG, "update: 收到会议更新事件" );
        meetInfo = event.getMeetInfo();
        int state = event.getState();
        tvMeetingName.setVisibility(View.VISIBLE);
        switch (state) {
            case MeetingEvent.INIT:

                break;
            case MeetingEvent.PRELOADING:
                tvMeetingName.setText(meetInfo.getName()+"   即将开始\n" + meetInfo.getBeginTime());
                break;
            case MeetingEvent.BEGINED:
                tvMeetingName.setVisibility(View.GONE);
                tvMeetingName.setText(meetInfo.getName()+"   已开始" + meetInfo.getBeginTime());
                break;
            case MeetingEvent.END:
                tvMeetingName.setText(meetInfo.getName()+"   已结束");

                break;
        }

        downloadIntroData();
    }

    private void downloadIntroData(){
        if(meetInfo == null){
            avLoading.setVisibility(View.GONE);
            tvMeetingName.setVisibility(View.VISIBLE);
            tvMeetingName.setText("暂无会议");
            return;
        }
        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetInfo.getId());
        if(advertInfos == null || advertInfos.size()<=0){
            tvMeetingName.setVisibility(View.VISIBLE);
            tvMeetingName.setText("暂无宣传资源");
            return;
        }

        //检测资源
        checkResource(advertInfos, new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        avLoading.setVisibility(View.GONE);

                        loadIntro();
                    }
                });
            }
        });
    }

    private void loadIntro(){
        List<String> urlList = new ArrayList<>();
        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetInfo.getId());
        if(advertInfos != null){
            for (AdvertInfo advertInfo : advertInfos) {
                Log.e(TAG, "loadIntro: ----- " + "添加到播放列表");
                urlList.add(advertInfo.getPath());
            }
        }

        pagerAdapter = new VerticalViewPagerAdapter(getChildFragmentManager());
        pagerAdapter.setUrlList(urlList);
        verticalViewPager.setAdapter(pagerAdapter);
    }

    private void checkResource(List<AdvertInfo> advertInfos,Runnable runnable){
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
                iterator.remove();
                continue;
            }
        }

        if(advertInfos.size() <= 0){
            runnable.run();
            return;
        }

        Queue<AdvertInfo> advertQueue = new ArrayDeque<>();
        advertQueue.addAll(advertInfos);

        download(advertQueue, runnable);
    }

    private void download(final Queue<AdvertInfo> entryQueue, final Runnable runnable) {
        if (entryQueue == null || entryQueue.size() <= 0) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }

        AdvertInfo advertInfo = entryQueue.poll();

        String head = advertInfo.getUrl();
        String headPath = advertInfo.getPath();

        Log.e(TAG,"准备下载： --- " + head + " --- " + headPath);

        MyXutils.getInstance().downLoadFile(head, headPath, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG,"下载完成：" + result.getPath());
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



    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
