package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.CurrFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.MediaFragment;
import com.yunbiao.yb_smart_meeting.activity.fragment.child.MeetingListFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import leifu.viewpagertransfomerlibrary.transformer.CubePageTransformer;

public class Intro2Fragment extends BaseFragment {
    private ViewPager viewPager;

    @Override
    protected int setLayout() {
        return R.layout.fragment_intro2;
    }

    @Override
    protected void initView() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MediaFragment());
        fragments.add(new CurrFragment());
        fragments.add(new MeetingListFragment());

        viewPager = find(R.id.vp);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(false, new CubePageTransformer(30F));//也可自定义动画范围大小new CubePageTransformer(90f)
        viewPager.setAdapter(new PageAdapter(getChildFragmentManager(),fragments));
    }

    @Override
    protected void initData() {

    }

    @Override
    public void update(MeetingEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MediaFragment.NoMediaDataEvent event){
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(1);
            }
        },1500);
    }

    class PageAdapter extends FragmentPagerAdapter {
        private List<Fragment> pageList ;
        public PageAdapter(FragmentManager manager,List<Fragment> fragmentList) {
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
            return ((Fragment)o).getView() == view;
        }
    }
}
