package com.yunbiao.yb_smart_meeting.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.Event.MeetingEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseFragment extends Fragment {

    private TextView tvTips;
    private View avlLoading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(setLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findLoadingView();
        initView();
        initData();
    }

    private void findLoadingView(){
        tvTips = find(R.id.tv_tips_fragment);
        avlLoading = find(R.id.avl_loading_fragment);
    }

    protected void showLoading(){
        avlLoading.post(new Runnable() {
            @Override
            public void run() {
                avlLoading.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void showTips(final String msg){
        hideLoading();
        tvTips.post(new Runnable() {
            @Override
            public void run() {
                tvTips.setVisibility(View.VISIBLE);
                tvTips.setText(msg);
            }
        });
    }

    protected void hideTips(){
        if(tvTips == null){
            return;
        }
        if(!tvTips.isShown()){
            return;
        }
        tvTips.post(new Runnable() {
            @Override
            public void run() {
                tvTips.setVisibility(View.GONE);
            }
        });
    }

    protected void hideLoading(){
        if(avlLoading == null){
            return;
        }
        avlLoading.post(new Runnable() {
            @Override
            public void run() {
                avlLoading.setVisibility(View.GONE);
            }
        });
    }

    protected void hideLoadingAndTips(){
        avlLoading.post(new Runnable() {
            @Override
            public void run() {
                tvTips.setVisibility(View.GONE);
                avlLoading.setVisibility(View.GONE);
            }
        });
    }

    protected abstract int setLayout();

    protected abstract void initView();

    protected abstract void initData();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(MeetingEvent event){

    }

    protected <T extends View> T find(int id){
        return getView().findViewById(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    protected void d(String log){
        Log.d(getClass().getSimpleName(), log);
    }
}
