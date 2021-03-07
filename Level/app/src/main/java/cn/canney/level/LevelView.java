package cn.canney.level;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;

/**
 * 水平仪控件
 * 通过设置{@link #setAngle(double, double)}
 * @author chen.canney@gmail.com
 */
public class LevelView extends View {

    /**
     * 最大圈半径
     */
    private float mLimitRadius = 0;

    /**
     * 气泡半径
     */
    private float mBubbleRadius;

    /**
     * 最大限制圈颜色
     */
    private int mLimitColor;

    /**
     * 限制圈宽度
     */
    private float mLimitCircleWidth;


    /**
     * 气泡中心标准圆颜色
     */
    private int mBubbleRuleColor;

    /**
     * 气泡中心标准圆宽
     */
    private float mBubbleRuleWidth;

    /**
     * 气泡中心标准圆半径
     */
    private float mBubbleRuleRadius;

    /**
     * 水平后的颜色
     */
    private int mHorizontalColor;

    /**
     * 气泡颜色
     */
    private int mBubbleColor;

    private Paint mBubblePaint;
    private Paint mLimitPaint;
    private Paint mBubbleRulePaint;

    /**
     * 中心点坐标
     */
    private PointF centerPnt = new PointF();

    /**
     * 计算后的气泡点
     */
    private PointF bubblePoint;
    private double pitchAngle = -90;
    private double rollAngle = -90;
    private Vibrator vibrator;

    public LevelView(Context context) {
        super(context);
        init(null, 0);
    }

    public LevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LevelView, defStyle, 0);

        mBubbleRuleColor = a.getColor(R.styleable.LevelView_bubbleRuleColor, mBubbleRuleColor);

        mBubbleColor = a.getColor(R.styleable.LevelView_bubbleColor, mBubbleColor);
        mLimitColor = a.getColor(R.styleable.LevelView_limitColor, mLimitColor);

        mHorizontalColor = a.getColor(R.styleable.LevelView_horizontalColor, mHorizontalColor);


        mLimitRadius = a.getDimension(R.styleable.LevelView_limitRadius, mLimitRadius);
        mBubbleRadius = a.getDimension(R.styleable.LevelView_bubbleRadius, mBubbleRadius);
        mLimitCircleWidth = a.getDimension(R.styleable.LevelView_limitCircleWidth, mLimitCircleWidth);

        mBubbleRuleWidth = a.getDimension(R.styleable.LevelView_bubbleRuleWidth, mBubbleRuleWidth);

        mBubbleRuleRadius = a.getDimension(R.styleable.LevelView_bubbleRuleRadius, mBubbleRuleRadius);


        a.recycle();


        mBubblePaint = new Paint();

        mBubblePaint.setColor(mBubbleColor);
        mBubblePaint.setStyle(Paint.Style.FILL);
        mBubblePaint.setAntiAlias(true);

        mLimitPaint = new Paint();

        mLimitPaint.setStyle(Paint.Style.STROKE);
        mLimitPaint.setColor(mLimitColor);
        mLimitPaint.setStrokeWidth(mLimitCircleWidth);
        //抗锯齿
        mLimitPaint.setAntiAlias(true);

        mBubbleRulePaint = new Paint();
        mBubbleRulePaint.setColor(mBubbleRuleColor);
        mBubbleRulePaint.setStyle(Paint.Style.STROKE);
        mBubbleRulePaint.setStrokeWidth(mBubbleRuleWidth);
        mBubbleRulePaint.setAntiAlias(true);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        calculateCenter(widthMeasureSpec, heightMeasureSpec);
    }

    private void calculateCenter(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.UNSPECIFIED);

        int height = MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.UNSPECIFIED);

        int center = Math.min(width, height) / 2;

        centerPnt.set(center, center);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean isCenter = isCenter(bubblePoint);
        int limitCircleColor = isCenter ? mHorizontalColor : mLimitColor;
        int bubbleColor = isCenter ? mHorizontalColor : mBubbleColor;

        //水平时振动
        if(isCenter){
            vibrator.vibrate(10);
        }

        mBubblePaint.setColor(bubbleColor);
        mLimitPaint.setColor(limitCircleColor);

        canvas.drawCircle(centerPnt.x, centerPnt.y, mBubbleRuleRadius, mBubbleRulePaint);
        canvas.drawCircle(centerPnt.x, centerPnt.y, mLimitRadius, mLimitPaint);

        drawBubble(canvas);

    }

    private boolean isCenter(PointF bubblePoint){

        if(bubblePoint == null){
            return false;
        }

        return Math.abs(bubblePoint.x - centerPnt.x) < 1 && Math.abs(bubblePoint.y - centerPnt.y) < 1;
    }

    private void drawBubble(Canvas canvas) {
        if(bubblePoint != null){
            canvas.drawCircle(bubblePoint.x, bubblePoint.y, mBubbleRadius, mBubblePaint);
        }
    }

    /**
     * Convert angle to screen coordinate point.
     * @param rollAngle 横滚角(弧度)
     * @param pitchAngle 俯仰角(弧度)
     * @return
     */
    private PointF convertCoordinate(double rollAngle, double pitchAngle, double radius){
        double scale = radius / Math.toRadians(90);

        //以圆心为原点，使用弧度表示坐标
        double x0 = -(rollAngle * scale);
        double y0 = -(pitchAngle * scale);

        //使用屏幕坐标表示气泡点
        double x = centerPnt.x - x0;
        double y = centerPnt.y - y0;

        return new PointF((float)x, (float)y);
    }

    /**
     *
     * @param pitchAngle （弧度）
     * @param rollAngle (弧度)
     */
    public void setAngle(double rollAngle, double pitchAngle) {

        this.pitchAngle = pitchAngle;
        this.rollAngle = rollAngle;

        //考虑气泡边界不超出限制圆，此处减去气泡的显示半径，做为最终的限制圆半径
        float limitRadius = mLimitRadius - mBubbleRadius;

        bubblePoint = convertCoordinate(rollAngle, pitchAngle, mLimitRadius);
        outLimit(bubblePoint, limitRadius);

        //坐标超出最大圆，取法向圆上的点
        if(outLimit(bubblePoint, limitRadius)){
            onCirclePoint(bubblePoint, limitRadius);
        }

        invalidate();
    }

    /**
     * 验证气泡点是否超过限制{@link #mLimitRadius}
     * @param bubblePnt
     * @return
     */
    private boolean outLimit(PointF bubblePnt, float limitRadius){

        float cSqrt = (bubblePnt.x - centerPnt.x)*(bubblePnt.x - centerPnt.x)
                + (centerPnt.y - bubblePnt.y) * + (centerPnt.y - bubblePnt.y);


        if(cSqrt - limitRadius * limitRadius > 0){
            return true;
        }
        return false;
    }

    /**
     * 计算圆心到 bubblePnt点在圆上的交点坐标
     * 即超出圆后的最大圆上坐标
     * @param bubblePnt 气泡点
     * @param limitRadius 限制圆的半径
     * @return
     */
    private PointF onCirclePoint(PointF bubblePnt, double limitRadius) {
        double azimuth = Math.atan2((bubblePnt.y - centerPnt.y), (bubblePnt.x - centerPnt.x));
        azimuth = azimuth < 0 ? 2 * Math.PI + azimuth : azimuth;

        //圆心+半径+角度 求圆上的坐标
        double x1 = centerPnt.x + limitRadius * Math.cos(azimuth);
        double y1 = centerPnt.y + limitRadius * Math.sin(azimuth);

        bubblePnt.set((float) x1, (float) y1);

        return bubblePnt;
    }

    public double getPitchAngle(){
        return this.pitchAngle;
    }

    public double getRollAngle(){
        return this.rollAngle;
    }


}

