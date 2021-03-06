package com.yunbiao.yb_smart_meeting.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/** * com.ykb.json.customview * 描述 :带扫描线的ImageView * 作者 : ykb * 时间 : 15/11/4. */
public class ScanningImageView extends ImageView {

    private static final int CHANGE_BOUNDS = 50;
    private Paint mPaint;
    private int mHeight = 0;
    private Path mPath;

    public ScanningImageView(Context context) {
        this(context, null);
    }

    public ScanningImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanningImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);//设置绘制的覆盖物不能超出背景的轮廓
        }
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setAlpha(255);
    }

    private boolean isRunning = false;

    public synchronized void start(){
        if(!isRunning){
            isRunning = true;
            postInvalidate();
        }
    }

    public void stop(){
        isRunning = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isRunning){
            mHeight += 10;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mPath.reset();
                canvas.clipPath(mPath);
                mPath.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CCW);
                canvas.clipPath(mPath, Region.Op.REPLACE);
            }
            LinearGradient linearGradient = new LinearGradient(0, mHeight - CHANGE_BOUNDS, 0, mHeight, new int[]{Color.TRANSPARENT, Color.WHITE}, null, Shader.TileMode.CLAMP);
            mPaint.setShader(linearGradient);
            canvas.drawRect(0, mHeight - CHANGE_BOUNDS, getWidth(), mHeight, mPaint);

            if (mHeight >= getHeight()) {
                mHeight = 0;
            }
            postInvalidateDelayed(40);
        }
        super.onDraw(canvas);
    }
}