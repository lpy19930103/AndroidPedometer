package com.lipy.step.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.lipy.step.R;
import com.lipy.step.utils.ScreenUtil;

import java.math.BigDecimal;

/**
 * 圆形进度条，类似 QQ 健康中运动步数的 UI 控件
 * Created by lipy on 2017/4/14 0014.
 */

public class StepProgress extends View {

    public static final boolean ANTI_ALIAS = true;//是否开启抗锯齿

    public static final int DEFAULT_SIZE = 150; //默认宽高

    public static final int DEFAULT_START_ANGLE = 135;//起始点

    public static final int DEFAULT_SWEEP_ANGLE = 270;//终止点

    public static final int DEFAULT_ANIM_TIME = 2000;//动画时常

    public static final int DEFAULT_MAX_VALUE = 8000;//最大进度

    public static final int DEFAULT_VALUE = 1;//默认初始进度

    public static final int DEFAULT_UNIT_SIZE = 30;//单位字体大小

    public static final int DEFAULT_VALUE_SIZE = 15;//进度字体大小

    public static final int DEFAULT_ARC_WIDTH = 15;//圆弧宽度

    private static final String TAG = StepProgress.class.getSimpleName();

    private Context mContext;

    private ProgressListener mProgressListener;

    //默认大小
    private int mDefaultSize;
    //是否开启抗锯齿
    private boolean antiAlias;

    //绘制单位
    private TextPaint mUnitPaint;
    private CharSequence mUnit;
    private int mUnitColor;
    private float mUnitSize;
    private float mUnitOffset;

    //绘制数值
    private TextPaint mValuePaint;
    private float mValue = 1;
    private float mMaxValue = 8000;
    private float mValueOffset;
    private int mPrecision;
    private String mPrecisionFormat;
    private int mValueColor;
    private float mValueSize;

    //绘制圆弧
    private Paint mArcPaint;
    private float mArcWidth;
    private float mStartAngle, mSweepAngle;
    private RectF mRectF;
    //渐变的颜色是360度，如果只显示270，那么则会缺失部分颜色
    private SweepGradient mSweepGradient;
    private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.RED};
    //当前进度，[0.0f,1.0f]
    private float mPercent = 0.0f;
    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;
    //绘制背景圆弧
    private Paint mBgArcPaint;
    private int mBgArcColor;
    private float mBgArcWidth;

    private int mDialIntervalDegree;
    //刻度线颜色
    private Paint mDialPaint;
    private float mDialWidth;
    private int mDialColor;


    //圆心坐标，半径
    private Point mCenterPoint;
    private float mRadius;
    private float mTextOffsetPercentInRadius;

    public StepProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = ScreenUtil.dipToPx(mContext, DEFAULT_SIZE);
        mAnimator = new ValueAnimator();
        mRectF = new RectF();
        mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
//        setProgress(mValue);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.AnnulusProgress);

        antiAlias = typedArray.getBoolean(R.styleable.AnnulusProgress_antiAlias, ANTI_ALIAS);

        mValue = typedArray.getFloat(R.styleable.AnnulusProgress_progress, DEFAULT_VALUE);
        mMaxValue = typedArray.getFloat(R.styleable.AnnulusProgress_maxValue, DEFAULT_MAX_VALUE);
        //内容数值精度格式
        mPrecision = typedArray.getInt(R.styleable.AnnulusProgress_precision, 0);
        mPrecisionFormat = getPrecisionFormat(mPrecision);
        mValueColor = typedArray.getColor(R.styleable.AnnulusProgress_valueColor, Color.BLACK);
        mValueSize = typedArray.getDimension(R.styleable.AnnulusProgress_valueSize, DEFAULT_VALUE_SIZE);
        mUnit = typedArray.getString(R.styleable.AnnulusProgress_unit);
        mUnitColor = typedArray.getColor(R.styleable.AnnulusProgress_unitColor, Color.BLACK);
        mUnitSize = typedArray.getDimension(R.styleable.AnnulusProgress_unitSize, DEFAULT_UNIT_SIZE);

        mArcWidth = typedArray.getDimension(R.styleable.AnnulusProgress_arcWidth, DEFAULT_ARC_WIDTH);
        mStartAngle = typedArray.getFloat(R.styleable.AnnulusProgress_startAngle, DEFAULT_START_ANGLE);
        mSweepAngle = typedArray.getFloat(R.styleable.AnnulusProgress_sweepAngle, DEFAULT_SWEEP_ANGLE);

        mBgArcColor = typedArray.getColor(R.styleable.AnnulusProgress_bgArcColor, Color.WHITE);
        mBgArcWidth = typedArray.getDimension(R.styleable.AnnulusProgress_bgArcWidth, DEFAULT_ARC_WIDTH);
        mTextOffsetPercentInRadius = typedArray.getFloat(R.styleable.AnnulusProgress_textOffsetPercentInRadius, 0.33f);


        mAnimTime = typedArray.getInt(R.styleable.AnnulusProgress_animTime, DEFAULT_ANIM_TIME);

        mDialIntervalDegree = typedArray.getInt(R.styleable.AnnulusProgress_dialIntervalDegree, 3);
        mDialWidth = typedArray.getDimension(R.styleable.AnnulusProgress_dialWidth, 2);
        mDialColor = typedArray.getColor(R.styleable.AnnulusProgress_dialColor, getResources().getColor(R.color.dialColor));

        int gradientArcColors = typedArray.getResourceId(R.styleable.AnnulusProgress_arcColors, 0);
        if (gradientArcColors != 0) {
            try {
                int[] gradientColors = getResources().getIntArray(gradientArcColors);
                if (gradientColors.length == 0) {//如果渐变色为数组为0，则尝试以单色读取色值
                    int color = getResources().getColor(gradientArcColors);
                    mGradientColors = new int[2];
                    mGradientColors[0] = color;
                    mGradientColors[1] = color;
                } else if (gradientColors.length == 1) {//如果渐变数组只有一种颜色，默认设为两种相同颜色
                    mGradientColors = new int[2];
                    mGradientColors[0] = gradientColors[0];
                    mGradientColors[1] = gradientColors[0];
                } else {
                    mGradientColors = gradientColors;
                }
            } catch (Resources.NotFoundException e) {
                throw new Resources.NotFoundException("the give resource not found.");
            }
        }

        typedArray.recycle();
    }

    private void initPaint() {
        mValuePaint = new TextPaint();
        mValuePaint.setAntiAlias(antiAlias);
        mValuePaint.setTextSize(mValueSize);
        mValuePaint.setColor(mValueColor);
        mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        mUnitPaint = new TextPaint();
        mUnitPaint.setAntiAlias(antiAlias);
        mUnitPaint.setTextSize(mUnitSize);
        mUnitPaint.setColor(mUnitColor);
        mUnitPaint.setTextAlign(Paint.Align.CENTER);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mDialPaint = new Paint();
        mDialPaint.setAntiAlias(antiAlias);
        mDialPaint.setColor(mDialColor);
        mDialPaint.setStrokeWidth(mDialWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(ScreenUtil.measure(widthMeasureSpec, mDefaultSize),
                ScreenUtil.measure(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //求最小值作为实际值
        int minSize = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth);
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围
        mRadius = minSize / 2;
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        //绘制圆弧的边界
        mRectF.left = mCenterPoint.x - mRadius - maxArcWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - maxArcWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + maxArcWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + maxArcWidth / 2;

        mValueOffset = mCenterPoint.y;
        mUnitOffset = mCenterPoint.y + mRadius * mTextOffsetPercentInRadius;
        updateArcPaint();
    }

    private float getBaselineOffsetFromY(Paint paint) {
        return ScreenUtil.measureTextHeight(paint) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDial(canvas);
        drawText(canvas);
        drawArc(canvas);
    }

    /**
     * 绘制内容文字
     */
    private void drawText(Canvas canvas) {
        canvas.drawText(String.format(mPrecisionFormat, mValue), mCenterPoint.x, mValueOffset, mValuePaint);

        if (mUnit != null) {
            canvas.drawText(mUnit.toString(), mCenterPoint.x, mUnitOffset, mUnitPaint);
        }
    }

    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        canvas.save();
        float currentAngle = mSweepAngle * mPercent;
//        Log.e(TAG, "drawArc: currentAngle = "+currentAngle);
        canvas.rotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
//        canvas.drawArc(mRectF, currentAngle, mSweepAngle - currentAngle, false, mBgArcPaint);
        canvas.drawArc(mRectF, 0, currentAngle, false, mArcPaint);
        canvas.restore();
    }

    private void drawDial(Canvas canvas) {
        int total = (int) (mSweepAngle / mDialIntervalDegree);
        canvas.save();
        canvas.rotate(mStartAngle, mCenterPoint.x, mCenterPoint.y);
        for (int i = 0; i <= total; i++) {
            canvas.drawLine(mCenterPoint.x + mRadius + 12, mCenterPoint.y, mCenterPoint.x + mRadius + mArcWidth - 15, mCenterPoint.y, mDialPaint);
            canvas.rotate(mDialIntervalDegree, mCenterPoint.x, mCenterPoint.y);
        }
        canvas.restore();
    }

    private int startColor = getResources().getColor(R.color.progressStartColor);
    private int endColor = getResources().getColor(R.color.progressEndColor);

    /**
     * 更新圆弧画笔
     */
    private void updateArcPaint() {
        // 设置渐变
        int[] mGradientColors = {startColor, endColor, startColor};
        mSweepGradient = new SweepGradient(mCenterPoint.x, mCenterPoint.y, mGradientColors, null);
        mArcPaint.setShader(mSweepGradient);
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public CharSequence getUnit() {
        return mUnit;
    }

    public void setUnit(CharSequence unit) {
        mUnit = unit;
    }

    public float getValue() {
        return mValue;
    }

    /**
     * 设置当前值
     */
    public void setProgress(float progress) {
        if (progress > mMaxValue) {
            progress = mMaxValue;
        }

        float start = mPercent;
        if (mMaxValue == 0) {
            mMaxValue = 8000;
        }
        BigDecimal bigDecimal1 = new BigDecimal(Float.toString(progress));
        BigDecimal bigDecimal2 = new BigDecimal(Float.toString(mMaxValue));
        float end = bigDecimal1.divide(bigDecimal2).floatValue();

        startAnimator(start, end, mAnimTime);
    }

    private void startAnimator(float start, float end, long animTime) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mValue = mPercent * mMaxValue;
//                Log.d(TAG, "onAnimationUpdate: percent = " + mPercent
//                        + ";currentAngle = " + (mSweepAngle * mPercent)
//                        + ";value = " + mValue);
                invalidate();
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mProgressListener != null) {
                    mProgressListener.start();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mProgressListener != null) {
                    mProgressListener.end();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    /**
     * 获取最大值
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 获取精度
     */
    public int getPrecision() {
        return mPrecision;
    }

    public void setPrecision(int precision) {
        mPrecision = precision;
        mPrecisionFormat = getPrecisionFormat(precision);
    }

    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * 设置渐变
     */
    public void setGradientColors(int[] gradientColors) {
        mGradientColors = gradientColors;
        updateArcPaint();
    }

    public long getAnimTime() {
        return mAnimTime;
    }

    public void setAnimTime(long animTime) {
        mAnimTime = animTime;
    }

    /**
     * 重置
     */
    public void reset() {
        startAnimator(mPercent, 0.0f, 1000L);
    }

    /**
     * 获取数值精度格式化字符串
     *
     * @param precision
     * @return
     */
    public static String getPrecisionFormat(int precision) {
        return "%." + precision + "f";
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }

    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    /**
     * 进度执行监听
     */
    public interface ProgressListener {
        void start();

        void end();
    }
}