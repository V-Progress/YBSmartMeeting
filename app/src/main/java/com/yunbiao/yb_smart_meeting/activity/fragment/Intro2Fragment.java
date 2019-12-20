package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.CurrFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.MediaFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.MeetingListFragment;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.yunbiao.yb_smart_meeting.db2.MeetInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import leifu.viewpagertransfomerlibrary.transformer.CubePageTransformer;

public class Intro2Fragment extends BaseFragment {
    private ViewPager viewPager;
    private View ivLeft;
    private View ivRight;
    List<Fragment> fragments = new ArrayList<>();
    private PageAdapter pageAdapter;
    private MediaFragment mediaFragment;
    private CurrFragment currFragment;
    private MeetingListFragment meetingListFragment;

    @Override
    protected int setLayout() {
        return R.layout.fragment_intro2;
    }

    @Override
    protected void initView() {
        ivLeft = find(R.id.iv_left);
        ivRight = find(R.id.iv_right);
        viewPager = find(R.id.vp);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(false, new CubePageTransformer(30F));//也可自定义动画范围大小new CubePageTransformer(90f)
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                showMode(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        ivLeft.setOnClickListener(onClickListener);
        ivRight.setOnClickListener(onClickListener);
        showMode(0);
    }

    //设置按钮显示模式
    private void showMode(int currIndex) {
        if (currIndex == 0) {
            ivLeft.setVisibility(View.GONE);
            ivRight.setVisibility(View.VISIBLE);
        } else if (currIndex == 2) {
            ivLeft.setVisibility(View.VISIBLE);
            ivRight.setVisibility(View.GONE);
        } else {
            ivLeft.setVisibility(View.VISIBLE);
            ivRight.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            click(v.getId() == R.id.iv_left);
        }
    };

    private void click(boolean isLeft) {
        int currentItem = viewPager.getCurrentItem();
        if (isLeft) {
            viewPager.setCurrentItem(currentItem - 1, true);
        } else {
            viewPager.setCurrentItem(currentItem + 1, true);
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void update(MeetingEvent event) {
        int state = event.getState();
        MeetInfo meetInfo = event.getMeetInfo();
        setContent(state, meetInfo);
    }

    public void setContent(int state, MeetInfo meetInfo) {
        hideTips();
        if (state == MeetingEvent.GET_COMPLETE_SUCESS) {
            if (fragments.size() > 0) {
                fragments.clear();
                if (pageAdapter != null) {
                    pageAdapter.notifyDataSetChanged();
                }
            }

            MeetInfo meetInfo1 = DaoManager.get().queryMeetInfoByNum(1);
            long id = meetInfo1.getId();
            mediaFragment = MediaFragment.instance("meetId", id);
            currFragment = CurrFragment.instance("meetId", id);
            meetingListFragment = MeetingListFragment.instance("meetId", id);
            fragments.add(mediaFragment);
            fragments.add(currFragment);
            fragments.add(meetingListFragment);
            pageAdapter = new PageAdapter(getChildFragmentManager(), fragments);
            viewPager.setAdapter(pageAdapter);
        } else {//如果会议为空则清除所有fragment
            fragments.clear();
            pageAdapter = new PageAdapter(getChildFragmentManager(), fragments);
            viewPager.setAdapter(pageAdapter);
            String tips = "";
            if (state == MeetingEvent.GET_NO_MEETING) {
                tips = "暂无会议";
            } else {
                tips = "获取失败";
            }
            showTips(tips);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MediaFragment.NoMediaDataEvent event) {
        if (viewPager != null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == 1) {
                return;
            }
            viewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewPager.setCurrentItem(1);
                }
            }, 1500);
        }
    }

    class PageAdapter extends FragmentPagerAdapter {
        private List<Fragment> pageList;

        public PageAdapter(FragmentManager manager, List<Fragment> fragmentList) {
            super(manager);
            pageList = fragmentList;
        }

        @Override
        public int getCount() {
            return pageList.size();
        }

        @Override
        public Fragment getItem(int i) {
            return pageList.get(i);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return ((Fragment) o).getView() == view;
        }
    }
}
