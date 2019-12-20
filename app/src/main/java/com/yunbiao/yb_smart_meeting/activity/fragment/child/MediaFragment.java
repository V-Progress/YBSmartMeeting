package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.os.Bundle;
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
    private View avlLoad;

    public static MediaFragment instance(String key, long meetId) {
        MediaFragment mediaFragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(key, meetId);
        mediaFragment.setArguments(bundle);
        return mediaFragment;
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_media;
    }

    @Override
    protected void initView() {
        rlvMedia = find(R.id.rlv_media);
        avlLoad = find(R.id.avl_load);

        ViewPagerLayoutManager viewPagerLayoutManager = new ViewPagerLayoutManager(getContext(), OrientationHelper.VERTICAL, false);
        rlvMedia.setLayoutManager(viewPagerLayoutManager);
        rlvMedia.setNestedScrollingEnabled(false);
        rlvMedia.setOnFlingListener(null);

        myAdapter = new MediaAdapter(getActivity(), advertInfos, rlvMedia);
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
//        long meetId = getArguments().getLong("meetId");
        MeetInfo meetInfo = DaoManager.get().queryMeetInfoByNum(1);
        long meetId = meetInfo.getId();
        List<AdvertInfo> advertInfos = DaoManager.get().queryAdvertByMeetId(meetId);
        if (advertInfos == null || advertInfos.size() <= 0) {
            showTips("暂无播放资源");
            EventBus.getDefault().post(new NoMediaDataEvent());
            return;
        }
        hideTips();

        loadAdvert(advertInfos);
    }

    private void loadAdvert(final List<AdvertInfo> advertInfos) {
        rlvMedia.setVisibility(View.GONE);
        avlLoad.setVisibility(View.VISIBLE);
        Downloader.checkResource(advertInfos, new Runnable() {
            @Override
            public void run() {
                d("资源已全部下载完成--------------");
                loadMediaList(advertInfos);
            }
        });
    }

    private void loadMediaList(final List<AdvertInfo> advertInfos) {
        d("开始加载列表-----------");
        rlvMedia.setVisibility(View.VISIBLE);
        avlLoad.setVisibility(View.GONE);
        this.advertInfos.addAll(advertInfos);
        myAdapter.notifyDataSetChanged();
    }

    public class NoMediaDataEvent {

    }
}
