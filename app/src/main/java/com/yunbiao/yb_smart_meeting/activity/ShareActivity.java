package com.yunbiao.yb_smart_meeting.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jdjr.mobilecert.CountDownTimerUtils;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.base.BaseActivity;
import com.yunbiao.yb_smart_meeting.utils.MyCountDownTimer;
import com.yunbiao.yb_smart_meeting.utils.UIUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ShareActivity extends BaseActivity {

    private ImageView ivShare;
    private SeekBar seekBar;
    private Disposable timer;
    private TextView tvShareTime;

    @Override
    protected String setTitle() {
        return null;
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_share;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_share;
    }

    @Override
    protected void initView() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.dimAmount=0.3f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        ivShare = find(R.id.iv_share);
        seekBar = find(R.id.sb_share_time);
        tvShareTime = find(R.id.tv_share_time);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void initData() {
        String shareUrl = null;
        Intent intent = getIntent();
        if (intent != null) {
            shareUrl = intent.getStringExtra("shareUrl");
        }

        if (TextUtils.isEmpty(shareUrl)) {
            UIUtils.showShort(this, "出现错误，请重试！");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
            return;
        }

        Glide.with(this).load(shareUrl).asBitmap().into(ivShare);

        seekBar.setMax(60);
        seekBar.setProgress(0);
        tvShareTime.setText(seekBar.getMax()+"秒后关闭");

        timer = Observable.intervalRange(seekBar.getProgress(), seekBar.getMax()+1, 0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong){
                        tvShareTime.setText((60 - aLong) +"秒后关闭");
                        seekBar.setProgress(aLong.intValue());
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run(){
                        finish();
                    }
                })
                .subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.dispose();
            timer = null;
        }
    }
}
