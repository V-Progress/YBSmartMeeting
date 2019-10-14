package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dingmouren.layoutmanagergroup.viewpager.OnViewPagerListener;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;
import com.yunbiao.yb_smart_meeting.adapter.MediaAdapter;
import com.yunbiao.yb_smart_meeting.business.Downloader;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MediaFragment extends BaseFragment {
    private static final String TAG = "MediaFragment";
    private RecyclerView rlvMedia;
    private MediaAdapter myAdapter;
    private List<AdvertInfo> advertInfos = new ArrayList<>();

    @Override
    protected int setLayout() {
        return R.layout.fragment_media;
    }

    @Override
    protected void initView() {
        rlvMedia = find(R.id.rlv_media);
        ViewPagerLayoutManager viewPagerLayoutManager = new ViewPagerLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        rlvMedia.setLayoutManager(viewPagerLayoutManager);
        rlvMedia.setNestedScrollingEnabled(false);
        rlvMedia.setOnFlingListener(null);

        myAdapter = new MediaAdapter(getActivity(), advertInfos,rlvMedia);
        myAdapter.bindData();

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rlvMedia);
        viewPagerLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                Log.e(TAG, "onInitComplete: 1111111111111111");
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "onPageRelease --- " + isNext + " ----- " + position);
                if (myAdapter != null) {
                    myAdapter.stop(position);
                }
            }

            private int mPosition = -2;

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                if (mPosition == position) {
                    return;
                }
                mPosition = position;
                Log.e(TAG, "onPageSelected --- " + position + " ----- " + isBottom);
                if (myAdapter != null) {
                    myAdapter.start(position);
                }
            }
        });
    }

    @Override
    protected void initData() {
        showLoading();
    }

    @Override
    public void update(MeetingEvent event) {
        MeetInfo meetInfo = event.getMeetInfo();
        int state = event.getState();
        advertInfos.clear();
        myAdapter.notifyDataSetChanged();

        switch (state) {
            case MeetingEvent.GET_MEETING_FAILED:
            case MeetingEvent.NO_MEETING:
                rlvMedia.setVisibility(View.GONE);
                showTips("暂无会议");
                break;
            case MeetingEvent.PRELOAD:
            case MeetingEvent.BEGAN:
                loadAdvert(meetInfo);
                break;
        }
    }

    private void loadAdvert(MeetInfo meetInfo) {
        rlvMedia.setVisibility(View.GONE);
        if (meetInfo == null) {
            showTips("暂无会议安排");
            return;
        }
        long id = meetInfo.getId();
        final List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(id);
        if (advertInfos == null || advertInfos.size() <= 0) {
            EventBus.getDefault().post(new NoMediaDataEvent());
            showTips("暂无宣传资源");
            return;
        }
        hideLoadingAndTips();

        d("打印全部广告资源--------------");
        for (AdvertInfo advertInfo : advertInfos) {
            d(advertInfo.toString());
        }

        showLoading();
        Downloader.checkResource(advertInfos, new Runnable() {
            @Override
            public void run() {
                hideLoadingAndTips();

                d("资源已全部下载完成--------------");
                loadMediaList(advertInfos);
            }
        });
    }

    private void loadMediaList(final List<AdvertInfo> advertInfos) {
        d("开始加载列表-----------");
        for (AdvertInfo advertInfo : advertInfos) {
            d(advertInfo.toString());
        }
        rlvMedia.setVisibility(View.VISIBLE);
        this.advertInfos.addAll(advertInfos);
        myAdapter.notifyDataSetChanged();
    }

    public class NoMediaDataEvent {

    }
}
