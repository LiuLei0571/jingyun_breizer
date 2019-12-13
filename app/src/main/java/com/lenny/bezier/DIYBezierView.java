package com.lenny.bezier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


public class DIYBezierView extends BaseView {

    public static final String BEZIER_CIRCLE_COLOR = "#1296db";    //绿色
    public static final String NATIVE_CIRCLE_COLOR = "#1296db";    //橙色
    public static final String SEL_POINT_COLOR = "#1296db";        //蓝色

    // 圆的中心点
    private PointF mCenterPoint;
    // 圆半径
    private float mRadius;

    // 控制点列表，顺序为：右上、右下、左下、左上
    private List<PointF> mControlPointList;

    // 选中的点集合，受 status 影响
    private final List<PointF> mCurSelectPointList = new ArrayList<>();

    // 控制点占半径的比例
    private float mRatio;

    private Path mPath;


    private Paint mPaint;

    // 有效触碰的范围
    private int mTouchRegionWidth;


    // 是否显示辅助线
    private boolean mIsShowHelpLine;

    // 触碰的x轴
    private float mLastX = -1;
    // 触碰的y轴
    private float mLastY = -1;

    public DIYBezierView(Context context) {
        super(context);
    }

    public DIYBezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DIYBezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 获取控制点
     *
     * @return
     */
    public List<PointF> getControlPointList() {
        return mControlPointList;
    }


    public void setIsShowHelpLine(boolean isShowHelpLine) {
        this.mIsShowHelpLine = isShowHelpLine;
        invalidate();
    }

    @Override
    protected void init(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        mRadius = width / 4;

//        LINE_WIDTH = dpToPx(2);
//        POINT_RADIO_WIDTH = dpToPx(4);
//        SEL_POINT_RADIO_WIDTH = dpToPx(6);
        mTouchRegionWidth = dpToPx(20);

        mCenterPoint = new PointF(0, 0);

        mControlPointList = new ArrayList<>();

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setAlpha(200);
        mPaint.setColor(Color.parseColor(BEZIER_CIRCLE_COLOR));
//        mPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        mIsShowHelpLine = true;

        mRatio = 0.55f;

        calculateControlPoint();
    }

    public void setBezierCircleColor(int color){
        mPaint.setColor(color);
    }
    public void setBezierCircleColor(String color) {
        mPaint.setColor(Color.parseColor(color));
    }

    public void reset() {
        calculateControlPoint();
        invalidate();
    }

    public void play() {
        calculateControlPointPlay();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        drawCoordinate(canvas);

        canvas.translate(mWidth / 2, mHeight / 2);

        mPath.reset();

        // 画圆
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                mPath.moveTo(mControlPointList.get(i * 3).x, mControlPointList.get(i * 3).y);
            } else {
                mPath.lineTo(mControlPointList.get(i * 3).x, mControlPointList.get(i * 3).y);
            }

            int endPointIndex;
            if (i == 3) {
                endPointIndex = 0;
            } else {
                endPointIndex = i * 3 + 3;
            }

            mPath.cubicTo(mControlPointList.get(i * 3 + 1).x, mControlPointList.get(i * 3 + 1).y,
                    mControlPointList.get(i * 3 + 2).x, mControlPointList.get(i * 3 + 2).y,
                    mControlPointList.get(endPointIndex).x, mControlPointList.get(endPointIndex).y);
        }
        // 绘制贝塞尔曲线
        canvas.drawPath(mPath, mPaint);

        // 不需要辅助线，则画完贝塞尔曲线就终止
        if (!mIsShowHelpLine) {
            return;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 触碰的坐标
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (selectControlPoint(x, y)) {
                    mLastX = x;
                    mLastY = y;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastX == -1 || mLastY == -1) {
                    return true;
                }

                // 计算偏移值
                float offsetX = x - mLastX;
                float offsetY = y - mLastY;

                for (PointF point : mCurSelectPointList) {
                    point.x = point.x + offsetX;
                    point.y = point.y + offsetY;
                }
//                }


                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                mCurSelectPointList.clear();
//                mSelPoint = null;
                mLastX = -1;
                mLastY = -1;
                break;
            default:
                break;
        }

        invalidate();

        return true;
    }

    /**
     * 是否在有效的触碰范围
     *
     * @param x
     * @param y
     * @return true 有选中；false 无选中
     */
    private boolean selectControlPoint(float x, float y) {

        // 选中的下标
        int selIndex = -1;

        for (int i = 0; i < mControlPointList.size(); ++i) {

            PointF controlPoint = mControlPointList.get(i);

            float resultX = controlPoint.x + mWidth / 2;
            float resultY = controlPoint.y + mHeight / 2;

            RectF pointRange = new RectF(resultX - mTouchRegionWidth + 2,
                    resultY - mTouchRegionWidth,
                    resultX + mTouchRegionWidth,
                    resultY + mTouchRegionWidth);

            if (pointRange.contains(x, y)) {
                selIndex = i;
                break;
            }
        }

        // 如果没有选中的就返回
        if (selIndex == -1) {
            return false;
        }

        // 清空之前的选中点
        mCurSelectPointList.clear();

//        mSelPoint = mControlPointList.get(selIndex);

        mCurSelectPointList.add(mControlPointList.get(selIndex));

        return true;

    }

    /**
     * 计算圆的控制点
     */
    private void calculateControlPoint() {
        // 计算 中间控制点到端点的距离
        float controlWidth = mRatio * mRadius;

        mControlPointList.clear();

        // 右上
        mControlPointList.add(new PointF(0, -mRadius));//
        mControlPointList.add(new PointF(controlWidth, -mRadius));
        mControlPointList.add(new PointF(mRadius, -controlWidth));

        // 右下
        mControlPointList.add(new PointF(mRadius, 0));
        mControlPointList.add(new PointF(mRadius, controlWidth));
        mControlPointList.add(new PointF(controlWidth, mRadius));

        // 左下
        mControlPointList.add(new PointF(0, mRadius));
        mControlPointList.add(new PointF(-controlWidth, mRadius));
        mControlPointList.add(new PointF(-mRadius, controlWidth));
        // 左上
        mControlPointList.add(new PointF(-mRadius, 0));
        mControlPointList.add(new PointF(-mRadius, -controlWidth));
        mControlPointList.add(new PointF(-controlWidth, -mRadius));

    }


    /**
     * 计算圆的控制点
     */
    private void calculateControlPointPlay() {
        // 计算 中间控制点到端点的距离
        float controlWidth = mRatio * mRadius;

        mControlPointList.clear();


        // 右上
        mControlPointList.add(new PointF((float) (Math.random() * 30), (float) (-mRadius - (Math.random() * 30))));//
        mControlPointList.add(new PointF((float) (controlWidth + Math.random() * 100), -mRadius));
        mControlPointList.add(new PointF((float) (mRadius + Math.random() * 50), -controlWidth));

        // 右下
        mControlPointList.add(new PointF((float) (Math.random() *30) + mRadius, 0));
        mControlPointList.add(new PointF((float) (mRadius + Math.random() * 60), controlWidth));
        mControlPointList.add(new PointF(controlWidth, (float) (mRadius + Math.random() * 70)));

        // 左下
        mControlPointList.add(new PointF((float) (Math.random() * 30), (float) (mRadius + (Math.random() * 30))));
        mControlPointList.add(new PointF((float) (-controlWidth - Math.random() * 100), mRadius));
        mControlPointList.add(new PointF(-mRadius, (float) (controlWidth + Math.random() * 100)));
        // 左上
        mControlPointList.add(new PointF(-mRadius - (float) (Math.random() * 40), (float) (Math.random() * 40)));
        mControlPointList.add(new PointF((float) (-mRadius - Math.random() * 110), -controlWidth));
        mControlPointList.add(new PointF(-controlWidth, (float) (-mRadius - Math.random() * 150)));

    }

}
