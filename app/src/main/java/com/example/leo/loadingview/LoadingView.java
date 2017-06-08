package com.example.leo.loadingview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by leo on 2017/5/31.
 */

public class LoadingView extends View {

    private static final int WHITE_COLOR = 0xfffde399;
    private static final int ORANGE_COLOR = 0xffffa800;
    private static final int TOTAL_PROGRESS = 100;
    private static final int LEAF_ROATE_TIME = 2000;

    private Resources mResources;
    private Paint mBitmapPaint;
    private Paint mWhitePaint;
    private Paint mOrangePaint;
    private Bitmap mLeafBitmap;
    private int mLeafWidth;
    private int mLeafHeight;
    private Bitmap mKuangBitmap;
    private int mKuangWidth;
    private int mKuangHeight;
    private int mAddTime;
    private int mProgressWidth;
    private int mLeftMargin;
    private int mRightMargin;
    private int mTotalWidth;
    private int mTotalHeight;
    private int mArcRadius;
    private int mCurrentProgressPosition;
    private int mArcRightLocation;
    private Rect mOuterRect;
    private Rect mOuterDestRect;
    private RectF mWhiteRect;
    private RectF mOrangeRect;
    private RectF mArcRect;
    private int mProgress;
    private int mLeafRoateTime = LEAF_ROATE_TIME;
    private List<leaf> mLeafList;

    // 中等振幅大小
    private static final int MIDDLE_AMPLITUDE = 13;
    // 中等振幅大小
    private int mMiddleAmplitude = MIDDLE_AMPLITUDE;
    // 不同类型之间的振幅差距
    private static final int AMPLITUDE_DISPARITY = 5;
    // 振幅差
    private int mAmplitudeDisparity = AMPLITUDE_DISPARITY;


    public LoadingView(Context context) {
        this(context,null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mResources = getResources();
        initPaint();
        initBitmap();
        LeafFactory factory = new LeafFactory();
        mLeafList = factory.createLeafs(LeafFactory.MAX_NUM);


    }

    private void initBitmap() {
        mLeafBitmap = BitmapFactory.decodeResource(mResources, R.drawable.leaf);
        mLeafWidth = mLeafBitmap.getWidth();
        mLeafHeight = mLeafBitmap.getHeight();
        mKuangBitmap = BitmapFactory.decodeResource(mResources, R.drawable.leaf_kuang);
        mKuangWidth = mKuangBitmap.getWidth();
        mKuangHeight = mKuangBitmap.getHeight();
    }

    private void initPaint() {
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setFilterBitmap(true);

        mWhitePaint = new Paint();
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setColor(WHITE_COLOR);

        mOrangePaint = new Paint();
        mOrangePaint.setAntiAlias(true);
        mOrangePaint.setColor(ORANGE_COLOR);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgressAndLeafs(canvas);
        canvas.drawBitmap(mKuangBitmap,mOuterRect,mOuterDestRect,mBitmapPaint);
        invalidate();
    }

    private void drawProgressAndLeafs(Canvas canvas) {
        if (mProgress >= TOTAL_PROGRESS){
            mProgress = TOTAL_PROGRESS;
        }
        mCurrentProgressPosition = mProgressWidth * mProgress/TOTAL_PROGRESS;
        if (mCurrentProgressPosition < mArcRadius){//进度小于左边半径
            canvas.drawArc(mArcRect,90,180,false,mWhitePaint);
            mWhiteRect.left = mArcRightLocation;
            canvas.drawRect(mWhiteRect,mWhitePaint);
            drawLeafs(canvas);
            int angle = (int) (Math.toDegrees(mArcRadius - mCurrentProgressPosition)/mArcRadius);
            int startAngle = 180 - angle;
            int sweepAngle = 2 * angle;
            canvas.drawArc(mArcRect,startAngle,sweepAngle,false,mOrangePaint);
        }else {
            mWhiteRect.left = mCurrentProgressPosition;
            canvas.drawRect(mWhiteRect,mWhitePaint);
            drawLeafs(canvas);
            canvas.drawArc(mArcRect,90,180,false,mOrangePaint);
            mOrangeRect.left = mArcRightLocation;
            mOrangeRect.right = mCurrentProgressPosition;
            canvas.drawRect(mOrangeRect,mOrangePaint);
        }
    }

    private void drawLeafs(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        for (int i = 0;i < mLeafList.size();i++){
            leaf leaf = mLeafList.get(i);
            if (currentTime > leaf.startTime && leaf.startTime != 0){
                getLeafLocation(leaf,currentTime);
                canvas.save();
                Matrix matrix = new Matrix();
                float transX = leaf.x;
                float transY = leaf.y;
                matrix.postTranslate(transX,transY);
                float rotateFraction = ((currentTime - leaf.startTime) % mLeafRoateTime) / mLeafRoateTime;
                int angle = (int) (rotateFraction * 360);
                int rotate = leaf.rotateDirection == 0? angle + leaf.rotateAngle:-angle + leaf.rotateAngle;
                matrix.postRotate(rotate,transX + mLeafWidth /2,transY + mLeafHeight /2);
                canvas.drawBitmap(mLeafBitmap,matrix,mBitmapPaint);
                canvas.restore();
            }
        }
    }

    private void getLeafLocation(leaf leaf, long currentTime) {
        long intervalTime = currentTime - leaf.startTime;
        if (intervalTime < 0){
            return;
        }
        if (intervalTime > mLeafFloatTime){
            leaf.startTime = System.currentTimeMillis() + new Random().nextInt((int) mLeafFloatTime);
        }
        float fraction = intervalTime /mLeafFloatTime;
        leaf.x = (int) (mProgressWidth * (1 - fraction));
        leaf.y = getLocationY(leaf);
    }

    private int getLocationY(leaf leaf) {
        // y = a(wx+q)+h;
        float w = (float) (Math.PI * 2 /mProgressWidth);
        float a = mMiddleAmplitude;
        switch (leaf.mType){
            case LITTLE:
                //小振幅 = 中振幅 - 振幅差；
                a = mMiddleAmplitude - mAmplitudeDisparity;
                break;
            case MIDDLE:
                a = mMiddleAmplitude;
                break;
            case BIG:
                a = mMiddleAmplitude + mAmplitudeDisparity;
                break;
        }

        return (int) (a * Math.sin(w * leaf.x) + mArcRadius * 2 / 3);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        mProgressWidth = mTotalWidth;
        mArcRadius = mProgressWidth/2;
        mArcRightLocation = mArcRadius;

        mOuterRect = new Rect(0,0,mKuangWidth,mKuangHeight);
        mOuterDestRect = new Rect(0,0,mTotalWidth,mTotalHeight);
        mWhiteRect = new RectF(mCurrentProgressPosition,0,mTotalWidth,mTotalHeight);
        mOrangeRect = new RectF(mArcRadius,0,mCurrentProgressPosition,mTotalHeight);
        mArcRect = new RectF(0,0,mArcRadius * 2,mTotalHeight);



    }

    private enum StartType{
        LITTLE,MIDDLE,BIG
    }
    private class leaf{
        int x,y;
        StartType mType;
        int rotateAngle;
        int rotateDirection;
        long startTime;
    }

    private static final long LEAF_FLOAT_TIME = 3000;
    private long mLeafFloatTime = LEAF_FLOAT_TIME;

    private class LeafFactory{
        private static final int MAX_NUM = 8;
        Random mRandom = new Random();

        public leaf createLeaf(){
            leaf leaf = new leaf();
            int randomType = mRandom.nextInt(3);
            switch (randomType){
                case 0:
                    leaf.mType = StartType.LITTLE;
                    break;
                case 1:
                    leaf.mType = StartType.MIDDLE;
                    break;
                case 2:
                    leaf.mType = StartType.BIG;
                    break;
            }
            leaf.rotateAngle = mRandom.nextInt(360);
            leaf.rotateDirection = mRandom.nextInt(2);
            mAddTime += mRandom.nextInt((int) (mLeafFloatTime * 2));
            leaf.startTime = System.currentTimeMillis() + mAddTime;

            return leaf;
        }

        public List<leaf> createLeafs(int num){
            List<leaf> leafs = new ArrayList<>();
            for (int i = 0;i < num;i++){
                leafs.add(createLeaf());
            }
            return leafs;
        }

    }

    public void setProgress(int progress){
        this.mProgress = progress;
        postInvalidate();
    }
}
