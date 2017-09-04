package com.example.wuzijing20170904;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by dell on 2017/9/4.
 */

public class ArrowMoveView extends View {

    private Paint mBackgroundCirclePaint;
    private Paint mFrontCirclePaint;
    private Paint mTextPaint;
    private Paint mArcPaint;
    private Bitmap mBitmap;
    private Bitmap mOverturnBitmap;
    private Canvas mBitmapCanvas;
    private Canvas mOverturnBitmapCanvas;
    private Matrix mMatrix;
    private Camera mCamera;
    private int mWidth = 400;
    private int mHeight = 400;
    private int mPadding = 20;
    private int mProgress = 0;
    private int mMaxProgress = 100;
    private int mRotateAngle = 0;
    private Runnable mRotateRunnable;
    private Runnable mCleaningRunnable;
    private boolean isRotating;
    private boolean isInital = false;
    private boolean isDescending;
    private boolean isIncreasing;
    private boolean isCleaning;

    public ArrowMoveView(Context context) {
        super(context);
        init();
    }

    public ArrowMoveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    public void init() {
        //绘制背景圆的画笔
        mBackgroundCirclePaint = new Paint();
        mBackgroundCirclePaint.setAntiAlias(true);
        mBackgroundCirclePaint.setColor(Color.argb(0xff, 0x10, 0x53, 0xff));

        //绘制旋转圆的画笔
        mFrontCirclePaint = new Paint();
        mFrontCirclePaint.setAntiAlias(true);
        mFrontCirclePaint.setColor(Color.argb(0xff, 0x5e, 0xae, 0xff));

        //绘制文字的画笔
        mTextPaint = new Paint();
        mTextPaint.getTextLocale();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(80);
        mTextPaint.setColor(Color.WHITE);

        //绘制进度条的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setStrokeWidth(12);
        mArcPaint.setStyle(Paint.Style.STROKE);

        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap); //将画布和Bitmap关联
        //旋转bitmap与画布
        mOverturnBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mOverturnBitmapCanvas = new Canvas(mOverturnBitmap);

        mMatrix = new Matrix();
        mCamera = new Camera();

        mRotateRunnable = new Runnable() {
            @Override
            public void run() {

                //如果当前是正在增加过程
                if (isIncreasing) {
                    Log.d("cylog", "mProgress:" + mProgress);
                    //当进度增加到某一个数值的时候，停止增加
                    if (mProgress >= 99) {
                        isIncreasing = false;
                    }
                    mProgress++;
                } else {
                    //如果增加过程结束，那么开始翻转
                    //如果mRotateAngle是大于90度的，表示bitmap已经翻转了90度，
                    //此时bitmap的内容变成镜像内容，为了不出现镜像效果，我们需要再转过180度，
                    //此时就变为正常的显示了，而这多转的180度在onDraw内会减去。
                    if (mRotateAngle > 90 && mRotateAngle < 180)
                        mRotateAngle = mRotateAngle + 3 + 180;
                        //如果mRotateAngle超过了180度，翻转过程完成
                    else if (mRotateAngle >= 180) {
                        isRotating = false;
                        isInital = true;
                        mRotateAngle = 0;
                        return;
                    } else
                        //每次角度增加3，这个可以微调，适当即可
                        mRotateAngle += 3;
                }
                invalidate();
                //25ms后再次调用该方法
                postDelayed(this, 25);
            }
        };

        mCleaningRunnable = new Runnable() {
            @Override
            public void run() {
                //如果当前进度超过某一数值，那么停止清理
                if (mProgress >= 100) {
                    isCleaning = false;
                    return;
                }
                //如果当前处于下降过程，mProgress不断减少，直到为0
                if (isDescending) {
                    mProgress--;
                    if (mProgress <= 0)
                        isDescending = false;
                } else {
                    mProgress++;
                }
                invalidate();
                postDelayed(this, 40);
            }
        };

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCleaning) return;

                isDescending = true;
                isCleaning = true;
                mProgress--;
                postDelayed(mCleaningRunnable, 40);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBitmapCanvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mBackgroundCirclePaint);
        mBitmapCanvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - mPadding, mTextPaint);

        mBitmapCanvas.save();
        //实例化一个矩形，该矩形的左上角和右下角坐标与原Bitmap并不重合，这是因为要使
        //进度条与最外面的圆有一定的间隙
        RectF rectF = new RectF(10, 10, mWidth - 10, mHeight - 10);
        //先将画布逆时针旋转90度，这样drawArc的起始角度就能从0度开始，省去不必要的麻烦
        mBitmapCanvas.rotate(-90, mWidth / 2, mHeight / 2);
        mBitmapCanvas.drawArc(rectF, 0, ((float) mProgress / mMaxProgress) * 360, false, mArcPaint);
        mBitmapCanvas.restore();
        canvas.drawBitmap(mBitmap, 0, 0, null);

        mOverturnBitmapCanvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - mPadding, mFrontCirclePaint);
        String text = (int) (((float) mProgress / mMaxProgress) * 100) + "%";
        //获取文本的宽度
        float textWidth = mTextPaint.measureText(text);
        //获取文本规格
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float baseLine = mHeight / 2 - (metrics.ascent + metrics.descent) / 2;
        mOverturnBitmapCanvas.drawText(text, mWidth / 2 - textWidth / 2, baseLine, mTextPaint);

        //如果当前正在旋转
        if (isRotating) {
            mCamera.save();
            //旋转角度
            mCamera.rotateY(mRotateAngle);
            //如果旋转角度大于或等于180度的时候，减去180度
            if (mRotateAngle >= 180) {
                mRotateAngle -= 180;
            }
            //根据Camera的操作来获得相应的矩阵
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            mMatrix.preTranslate(-mWidth / 2, -mHeight / 2);
            mMatrix.postTranslate(mWidth / 2, mHeight / 2);
        }

        canvas.drawBitmap(mOverturnBitmap, mMatrix, null);

        //如果当前控件尚未进行翻转过程
        if (!isRotating && !isInital) {
            //设置isIncreasing，表示先开始进度条的增加过程
            isIncreasing = true;
            isRotating = true;
            postDelayed(mRotateRunnable, 10);
        }
    }
}
