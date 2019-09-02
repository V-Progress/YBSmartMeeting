package com.yunbiao.yb_smart_meeting.activity.fragment.child;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.ShareActivity;
import com.yunbiao.yb_smart_meeting.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_meeting.db2.AdvertInfo;
import com.yunbiao.yb_smart_meeting.db2.DaoManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<AdvertInfo> list;
    private Activity ctx;
    private List<VH> vhList = new ArrayList<>();
    private RecyclerView mRlv;
    private SwitchListener mListener = new SwitchListener() {
        @Override
        public void switchPosition(int position) {
            position+=1;
            if(position >= list.size()){
                position = 0;
            }
            mRlv.smoothScrollToPosition(position);
            Log.e(TAG, "当前：" + position + " ----- 切换到：" + (position + 1));
        }
    };

    public interface SwitchListener{
        void switchPosition(int position);
    }

    public MediaAdapter(Activity context, List<AdvertInfo> advertInfos,RecyclerView recyclerView) {
        ctx = context;
        list = advertInfos;
        mRlv = recyclerView;
    }

    public void bindData(){
        mRlv.setAdapter(this);
    }

    public void stop(int position){
        vhList.get(position).stop();
    }

    public void start(int position){
        vhList.get(position).start();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        VH vh = new VH(ctx, LayoutInflater.from(ctx).inflate(R.layout.item_layout_advert, viewGroup,false));
        vhList.add(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((VH) viewHolder).bindData(list.get(i),i);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class VH extends RecyclerView.ViewHolder {
        private Context ctx;
        private AdvertInfo data;
        private int position = 0;
        VideoView videoView;
        ImageView imageView;
        TextView tvWatch;
        TextView tvThumbs;
        ImageView ivThumbs;
        ImageView ivForward;
        ProgressBar pbThubms;
        private final View flRoot;

        public VH(Context context, @NonNull View itemView) {
            super(itemView);
            ctx = context;
            flRoot = itemView.findViewById(R.id.fl_advert_root);
            videoView = itemView.findViewById(R.id.vv_advert);
            imageView = itemView.findViewById(R.id.iv_advert);
            tvWatch = itemView.findViewById(R.id.tv_advert_watch);
            tvThumbs = itemView.findViewById(R.id.tv_advert_thumbs);
            ivThumbs = itemView.findViewById(R.id.iv_advert_thumbs);
            ivForward = itemView.findViewById(R.id.iv_advert_forward);
            pbThubms = itemView.findViewById(R.id.pb_advert_thumbs);
        }

        public void bindData(final AdvertInfo advertInfo,int posi) {
            data = advertInfo;
            position = posi;
            handleData();
        }

        private void handleData(){
            int type = data.getType();
            String path = data.getPath();
            if (type == 1) {
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(ctx).load(path).asBitmap().into(imageView);
                bindImageCallback(true);
            } else {
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                videoView.setVideoPath(data.getPath());
                bindVideoCallback(true);
            }

            updateWatch(data);
            updateThumbs(data);
            clickForward(data);
        }

        private void updateWatch(final AdvertInfo advertInfo) {
            tvWatch.setText(advertInfo.getReadNum() + "");
            OkHttpUtils.post().url(ResourceUpdate.UPDATE_WATCH)
                    .addParams("advertId", advertInfo.getAdvertId() + "")
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(String response, int id) {
                    advertInfo.setReadNum(advertInfo.getReadNum() + 1);
                    tvWatch.setText(advertInfo.getReadNum() + "");
                    DaoManager.get().addOrUpdate(advertInfo);
                }
            });
        }

        private void updateThumbs(final AdvertInfo advertInfo) {
            tvThumbs.setText(advertInfo.getGoodNum() + "");
            View.OnTouchListener onTouchListener = new View.OnTouchListener() {
                GestureDetector mGesture;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mGesture == null) {
                        mGesture = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent e) {

                                return true;
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {
                                super.onLongPress(e);
                            }

                            @Override
                            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                                return super.onScroll(e1, e2, distanceX, distanceY);
                            }
                        });
                        mGesture.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                            @Override
                            public boolean onSingleTapConfirmed(MotionEvent e) {
                                //返回false的话只能响应长摁事件
                                OkHttpUtils.post().url(ResourceUpdate.CLICK_THUMBS).addParams("advertId", advertInfo.getAdvertId() + "").build().execute(new StringCallback() {
                                    @Override
                                    public void onBefore(Request request, int id) {
                                        pbThubms.setVisibility(View.VISIBLE);
                                        ivThumbs.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Call call, Exception e, int id) {
                                        d("发送错误 --- " + (e == null ? " NULL " : e.getMessage()));
                                    }

                                    @Override
                                    public void onResponse(String response, int id) {
                                        d("点赞成功" + response);
                                        advertInfo.setGoodNum(advertInfo.getGoodNum() + 1);
                                        tvThumbs.setText(advertInfo.getGoodNum() + "");
                                        DaoManager.get().addOrUpdate(advertInfo);
                                    }

                                    @Override
                                    public void onAfter(int id) {
                                        pbThubms.setVisibility(View.GONE);
                                        ivThumbs.setVisibility(View.VISIBLE);
                                    }
                                });
                                return true;
                            }

                            @Override
                            public boolean onDoubleTap(MotionEvent e) {
                                return true;
                            }

                            @Override
                            public boolean onDoubleTapEvent(MotionEvent e) {
                                return false;
                            }
                        });
                    }

                    return mGesture.onTouchEvent(event);
                }
            };

            videoView.setOnTouchListener(onTouchListener);
            imageView.setOnTouchListener(onTouchListener);
            ivThumbs.setOnTouchListener(onTouchListener);
        }

        private void clickForward(final AdvertInfo advertInfo) {
            ivForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, ShareActivity.class);
                    intent.putExtra("shareUrl",advertInfo.getShareUrl());
                    ctx.startActivity(intent);
                }
            });
        }

        public void start() {
            updateWatch(data);
            if(data.getType() == 2){
                videoView.start();
                bindVideoCallback(true);
            } else {
                bindImageCallback(true);
            }
        }

        public void stop() {
            if(data.getType() == 2){
                videoView.stopPlayback();
                bindVideoCallback(false);
            } else {
                bindImageCallback(false);
            }
        }

        private void bindVideoCallback(boolean is){
            if(is){
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(mListener != null){
                            mListener.switchPosition(position);
                        }
                    }
                });
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        if(mListener != null){
                            mListener.switchPosition(position);
                        }
                        return true;
                    }
                });
            } else {
                videoView.setOnErrorListener(null);
                videoView.setOnCompletionListener(null);
            }
        }

        private void bindImageCallback(boolean is){
            imageView.removeCallbacks(runnable);
            if(is){
                imageView.postDelayed(runnable,data.getTime() * 1000);
            }
        }

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mListener != null){
                    mListener.switchPosition(position);
                }
            }
        };
    }

    private static final String TAG = "MediaAdapter";
    private void d(String log){
        Log.d(TAG, log);
    }
}
