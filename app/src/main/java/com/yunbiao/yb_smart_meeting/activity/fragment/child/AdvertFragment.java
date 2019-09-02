package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.fragment.BaseFragment;

public class AdvertFragment extends BaseFragment {
    private static final String TAG = "AdvertFragment";
    private static final String ARG_PARAM1 = "resourcePath";
    private static final String ARG_PARAM2 = "resourceUrl";

    private String path;
    private String url;
    private TextView view;

    @Override
    protected int setLayout() {
        return R.layout.fragment_advert;
    }

    // TODO: Rename and change types and number of parameters
    public static AdvertFragment newInstance(String resourcePath,String resourceUrl) {
        AdvertFragment fragment = new AdvertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, resourcePath);
        args.putString(ARG_PARAM2, resourceUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initView() {
        view = find(R.id.tv);
    }

    @Override
    protected void initData() {
        if (getArguments() != null) {
            path = getArguments().getString(ARG_PARAM1);
            url = getArguments().getString(ARG_PARAM2);
            Log.e(TAG, "initData: " + path);
            Log.e(TAG, "initData: " + url);
            view.setText(path+"\n"+url);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e(TAG, "setUserVisibleHint: ---- " + isVisibleToUser);
    }
}
