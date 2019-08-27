package com.yunbiao.yb_smart_meeting.views;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yunbiao.yb_smart_meeting.R;

public class VideoFragment extends Fragment {
    private String url;
    public static final String URL = "URL";
    private TextureVideoView tvv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_video, container, false);
        tvv = inflate.findViewById(R.id.tvv);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments != null){
            url = arguments.getString(URL);
            tvv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    tvv.setVideoPath(url);
                    tvv.start();
                }
            });
        }
    }


    protected void loadData() {
        tvv.setVideoPath(url);
        tvv.start();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (tvv == null) {
            return;
        }
        if (isVisibleToUser) {
            tvv.resume();
        } else {
            tvv.pause();
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        if (tvv != null) {
            tvv.resume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (tvv != null) {
            tvv.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tvv != null) {
            // true代表清除最后一帧画面
            tvv.stopPlayback();
        }
    }


}