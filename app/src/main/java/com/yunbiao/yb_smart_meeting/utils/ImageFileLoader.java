package com.yunbiao.yb_smart_meeting.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.yunbiao.yb_smart_meeting.R;
import com.yunbiao.yb_smart_meeting.activity.WelComeActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ImageFileLoader {
    private final String TAG = "ImageFileLoader";
    private ScheduledExecutorService executor;
    private static ImageFileLoader loader = new ImageFileLoader();
    private int defaultLogoId = R.mipmap.logo_yunbiao;

    private ImageFileLoader() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public static ImageFileLoader i() {
        return loader;
    }

    public void onlyShow(final Activity context, final String url, final ImageView imageView){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).load(url).asBitmap().override(150, 150).into(imageView);
            }
        });
    }

    public void loadAndSave(Context context, String url, String localDir, final ImageView imageView) {
        load(context, url, localDir, imageView, true);
    }

    /*
     * 截取该url的后几位生成文件名
     * 与本地路径结合判断是否存在，缓存标识
     * 如果不存在则加载，加载完成后保存到本地
     * 如果存在则设置并且不再缓存
     * */
    public synchronized void load(final Context context, String url, String localDir, final ImageView imageView, final boolean needSave) {
        executor.execute(new LoadRunnable(context, url, localDir, imageView, needSave,defaultLogoId));
    }

    private class LoadRunnable implements Runnable {
        private Context ctx;
        private String imgUrl;
        private String savePath;
        private ImageView imgView;
        private boolean saveTag;
        private int defaultImgId;

        public LoadRunnable(Context ctx, String imgUrl, String savePath, ImageView imgView, boolean saveTag,int defaultId) {
            this.ctx = ctx;
            this.imgUrl = imgUrl;
            this.savePath = savePath;
            this.imgView = imgView;
            this.saveTag = saveTag;
            this.defaultImgId = defaultId;
        }

        @Override
        public void run() {
            String name = getNameByUrl(imgUrl);
            if (TextUtils.isEmpty(name)) {
                e("load failed, url error!");
                imgView.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(ctx).load(defaultImgId).asBitmap().into(imgView);
                    }
                });
                return;
            }

            d(imgView.getId() + " --- " + "开始加载图片：" + imgUrl);

            final File file = new File(savePath, name);
            d(imgView.getId() + " --- " + (file.exists() ? "文件存在：" + file.getPath() : "文件不存在"));
            final String loadUrl = file.exists() ? file.getPath() : imgUrl;

            d(imgView.getId() + " --- " + "加载地址：" + loadUrl);
            try {
                d(imgView.getId() + " --- " + "加载地址：" + loadUrl);
                final Bitmap bitmap = Glide.with(ctx).load(loadUrl).asBitmap().error(defaultImgId).skipMemoryCache(true).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get(60, TimeUnit.SECONDS);
                if (bitmap != null) {
                    d(imgView.getId() + " --- " + "加载成功：" + bitmap.getWidth() + " : " + bitmap.getHeight());
                    imgView.post(new Runnable() {
                        @Override
                        public void run() {
//                            recycleImageView(imgView);
                            imgView.setImageBitmap(bitmap);
                        }
                    });
                    if (saveTag && !file.exists())
                        saveBitmap(imgView, bitmap, file.getPath());
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private void recycleImageView(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private void saveBitmap(ImageView imageView, Bitmap bitmap, String localPath) {
        d(imageView.getId() + " --- " + "保存图片：" + localPath);
        try {
            if (new File(localPath).exists()) {
                return;
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localPath));
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            bos.flush();
            bos.close();
            d(imageView.getId() + " --- " + "保存成功");
        } catch (IOException e) {
            e.printStackTrace();
            d(imageView.getId() + " --- " + "保存失败");
        }
    }

    private String getNameByUrl(String url) {
        return TextUtils.isEmpty(url) ? null :url.substring(url.lastIndexOf("/") + 1);
    }

    private void d(String log) {
        Log.d(TAG, log);
    }

    private void e(String log) {
        Log.e(TAG, log);
    }
}
