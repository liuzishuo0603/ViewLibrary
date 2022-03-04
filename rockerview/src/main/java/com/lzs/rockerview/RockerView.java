package com.lzs.rockerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * title：RockerView
 * author: zsliu
 * created by Administrator on 2022/2/11 18:45
 * description: 遥控杆View
 */
public class RockerView extends View {
    private final String TAG = "RockerView";
    private Paint mAreaBackgroundPaint;
    private Paint mRockerPaint;
    private Paint mTrianglePaint;
    private Point mCenterPoint;
    private Point mRockerPosition;
    private Path mTrianglePath;
    // 摇杆可移动区域背景
    private static final int AREA_BACKGROUND_MODE_PIC = 0;
    private static final int AREA_BACKGROUND_MODE_COLOR = 1;
    private static final int AREA_BACKGROUND_MODE_XML = 2;
    private static final int AREA_BACKGROUND_MODE_DEFAULT = 3;
    private int mAreaBackgroundMode = AREA_BACKGROUND_MODE_DEFAULT;
    // 摇杆背景
    private static final int ROCKER_BACKGROUND_MODE_PIC = 4;
    private static final int ROCKER_BACKGROUND_MODE_COLOR = 5;
    private static final int ROCKER_BACKGROUND_MODE_XML = 6;
    private static final int ROCKER_BACKGROUND_MODE_DEFAULT = 7;
    private int mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
    private int mAreaColor;
    private Bitmap mAreaBitmap;
    private Bitmap mRockerBitmap;
    private int mRockerColor;
    private OnAngleChangeListener mAngleChangeListener;
    private OnDistanceLevelListener mDistanceLevelListener;
    private float mRockerScale = 0f;
    //分成10分
    private int mDistanceLevel = 10;
    private CallBackMode mCallBackMode = CallBackMode.CALL_BACK_MODE_MOVE;

    private static final int DEFAULT_SIZE = 400;
    private static final float DEFAULT_ROCKER_SCALE = 0.3f;//默认半径为背景的1/2
    private int mAreaRadius;
    private int mRockerRadius;
    private DirectionMode mDirectionMode = DirectionMode.DIRECTION_4_ROTATE_45;
    private OnShakeListener mShakeListener;
    private Direction tempDirection = Direction.DIRECTION_CENTER;
    private int baseDistance = 0;
    private float lastDistance = 0;

    // 角度
    private static final double ANGLE_0 = 0;
    private static final double ANGLE_360 = 360;
    // 360°水平方向平分2份的边缘角度
    private static final double ANGLE_HORIZONTAL_2D_OF_0P = 90;
    private static final double ANGLE_HORIZONTAL_2D_OF_1P = 270;
    // 360°垂直方向平分2份的边缘角度
    private static final double ANGLE_VERTICAL_2D_OF_0P = 0;
    private static final double ANGLE_VERTICAL_2D_OF_1P = 180;
    // 360°平分4份的边缘角度
    private static final double ANGLE_4D_OF_0P = 0;
    private static final double ANGLE_4D_OF_1P = 90;
    private static final double ANGLE_4D_OF_2P = 180;
    private static final double ANGLE_4D_OF_3P = 270;
    // 360°平分4份的边缘角度(旋转45度)
    private static final double ANGLE_ROTATE45_4D_OF_0P = 45;
    private static final double ANGLE_ROTATE45_4D_OF_1P = 135;
    private static final double ANGLE_ROTATE45_4D_OF_2P = 225;
    private static final double ANGLE_ROTATE45_4D_OF_3P = 315;
    // 360°平分8份的边缘角度
    private static final double ANGLE_8D_OF_0P = 22.5;
    private static final double ANGLE_8D_OF_1P = 67.5;
    private static final double ANGLE_8D_OF_2P = 112.5;
    private static final double ANGLE_8D_OF_3P = 157.5;
    private static final double ANGLE_8D_OF_4P = 202.5;
    private static final double ANGLE_8D_OF_5P = 247.5;
    private static final double ANGLE_8D_OF_6P = 292.5;
    private static final double ANGLE_8D_OF_7P = 337.5;
    private OnDirectionListener mDirectionListener;

    public RockerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 获取自定义属性
        initAttribute(context, attrs);
        //创建移动区域画笔
        mAreaBackgroundPaint = new Paint();
        //设置抗锯齿集
        mAreaBackgroundPaint.setAntiAlias(true);
        //创建摇杆
        mRockerPaint = new Paint();
        mRockerPaint.setAntiAlias(true);
        //创建三角画笔
        mTrianglePaint = new Paint();
        mTrianglePaint.setAntiAlias(true);
        //创建中心点
        mCenterPoint = new Point();
        //创建摇杆位置
        mRockerPosition = new Point();
        //创建三角位置
        mTrianglePath = new Path();
    }

    /**
     * 初始化自定义属性
     *
     * @param context 上下文
     * @param attrs   属性
     */
    @SuppressLint("Recycle")
    private void initAttribute(Context context, AttributeSet attrs) {
        //获取定义属性参数
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerView);
        // 可移动区域背景
        Drawable areaBackground = typedArray.getDrawable(R.styleable.RockerView_areaBackground);
        if (areaBackground != null) {//设置背景
            if (areaBackground instanceof BitmapDrawable) {//图片绘制模式
                // 设置了一张图片
                mAreaBitmap = ((BitmapDrawable) areaBackground).getBitmap();
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_PIC;
            } else if (areaBackground instanceof GradientDrawable) {//渐变
                // XML
                mAreaBitmap = drawable2Bitmap(areaBackground);
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_XML;
            } else if (areaBackground instanceof ColorDrawable) {//颜色
                // 色值
                mAreaColor = ((ColorDrawable) areaBackground).getColor();
                mAreaBackgroundMode = AREA_BACKGROUND_MODE_COLOR;
            } else {
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
            }
        }
        //摇杆背景
        Drawable rockerBackground = typedArray.getDrawable(R.styleable.RockerView_rockerBackground);
        if (rockerBackground != null) {
            // 设置了摇杆背景
            if (rockerBackground instanceof BitmapDrawable) {
                // 图片
                mRockerBitmap = ((BitmapDrawable) rockerBackground).getBitmap();
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_PIC;
            } else if (rockerBackground instanceof GradientDrawable) {
                // XML
                mRockerBitmap = drawable2Bitmap(rockerBackground);
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_XML;
            } else if (rockerBackground instanceof ColorDrawable) {
                // 色值
                mRockerColor = ((ColorDrawable) rockerBackground).getColor();
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_COLOR;
            } else {
                // 其他形式
                mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
            }
        } else {
            // 没有设置摇杆背景
            mRockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT;
        }
        // 摇杆半径
        mRockerScale = typedArray.getFloat(R.styleable.RockerView_rockerScale, DEFAULT_ROCKER_SCALE);
        //距离级别
        mDistanceLevel = typedArray.getInt(R.styleable.RockerView_rockerSpeedLevel, 10);
        //回调模式
        mCallBackMode = getCallBackMode(typedArray.getInt(R.styleable.RockerView_rockerCallBackMode, 0));
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measureWidth = getMeasuredWidth();
        int measureHeight = getMeasuredHeight();
        int cx = measureWidth / 2;
        int cy = measureHeight / 2;
        // 中心点
        mCenterPoint.set(cx, cy);
        // 可移动区域的半径
        mAreaRadius = (measureWidth <= measureHeight) ? (int) (cx / (mRockerScale + 1)) : (int) (cy / (mRockerScale + 1));
        mRockerRadius = (int) (mAreaRadius * mRockerScale);
        // 摇杆位置
        if (0 == mRockerPosition.x || 0 == mRockerPosition.y) {
            mRockerPosition.set(mCenterPoint.x, mCenterPoint.y);
        }
        // 画可移动区域
        if (AREA_BACKGROUND_MODE_PIC == mAreaBackgroundMode || AREA_BACKGROUND_MODE_XML == mAreaBackgroundMode) {
            // 图片
            Rect src = new Rect(0, 0, mAreaBitmap.getWidth(), mAreaBitmap.getHeight());
            Rect dst = new Rect(mCenterPoint.x - mAreaRadius, mCenterPoint.y - mAreaRadius, mCenterPoint.x + mAreaRadius, mCenterPoint.y + mAreaRadius);
            canvas.drawBitmap(mAreaBitmap, src, dst, mAreaBackgroundPaint);
        } else if (AREA_BACKGROUND_MODE_COLOR == mAreaBackgroundMode) {
            // 色值
            mAreaBackgroundPaint.setColor(mAreaColor);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mAreaRadius, mAreaBackgroundPaint);
        } else {
            // 其他或者未设置
            mAreaBackgroundPaint.setColor(Color.GRAY);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mAreaRadius, mAreaBackgroundPaint);
        }
        // 画摇杆
        if (ROCKER_BACKGROUND_MODE_PIC == mRockerBackgroundMode || ROCKER_BACKGROUND_MODE_XML == mRockerBackgroundMode) {
            // 图片
            Rect src = new Rect(0, 0, mRockerBitmap.getWidth(), mRockerBitmap.getHeight());
            Rect dst = new Rect(mRockerPosition.x - mRockerRadius, mRockerPosition.y - mRockerRadius, mRockerPosition.x + mRockerRadius, mRockerPosition.y + mRockerRadius);
            canvas.drawBitmap(mRockerBitmap, src, dst, mRockerPaint);
        } else if (ROCKER_BACKGROUND_MODE_COLOR == mRockerBackgroundMode) {
            // 色值
            mRockerPaint.setColor(mRockerColor);
            canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mRockerPaint);
        } else {
            // 其他或者未设置
            mRockerPaint.setColor(Color.BLUE);
            canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mRockerPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth, measureHeight;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//宽度模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);//高度模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//宽度尺寸
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);//高度尺寸
        if (widthMode == MeasureSpec.EXACTLY) {
            // 具体的值和match_parent
            measureWidth = widthSize;
        } else {
            // wrap_content
            measureWidth = DEFAULT_SIZE;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = heightSize;
        } else {
            measureHeight = DEFAULT_SIZE;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * 触摸事件
     *
     * @param event 事件
     * @return 事件触发
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下
                //启动回调
                callBackStart();
            case MotionEvent.ACTION_MOVE:// 移动
                float moveX = event.getX();
                float moveY = event.getY();
                baseDistance = mAreaRadius + 2;
                mRockerPosition = getRockerPositionPoint(mCenterPoint, new Point((int) moveX, (int) moveY), mAreaRadius + mRockerRadius, mRockerRadius);
                moveRocker(mRockerPosition.x, mRockerPosition.y);
                break;
            case MotionEvent.ACTION_UP:// 抬起

            case MotionEvent.ACTION_CANCEL:// 移出区域
                //结束回调
                callBackFinish();
                if (mShakeListener != null) {
                    mShakeListener.direction(Direction.DIRECTION_CENTER);
                }
                moveRocker(mCenterPoint.x, mCenterPoint.y);
                break;
        }
        return true;
    }

    /**
     * 移动摇杆到指定位置
     *
     * @param x x坐标
     * @param y y坐标
     */
    private void moveRocker(int x, int y) {
        mRockerPosition.set(x, y);
        invalidate();
    }

    private void t() {
        mTrianglePaint.setColor(Color.YELLOW);
        mTrianglePath.moveTo(mRockerPosition.x, mRockerPosition.y);// 此点为多边形的起点
        mTrianglePath.lineTo(100, 100);
        mTrianglePath.lineTo(100, 350);
        mTrianglePath.close();
        invalidate();
//        canvas.drawPath(mTrianglePath, mTrianglePaint);
    }

    /**
     * 获取摇杆实际要显示的位置（点）
     *
     * @param centerPoint  中心点
     * @param touchPoint   触摸点
     * @param regionRadius 摇杆可活动区域半径
     * @param rockerRadius 摇杆半径
     * @return 摇杆实际显示的位置（点）
     */
    private Point getRockerPositionPoint(Point centerPoint, Point touchPoint, float regionRadius, float rockerRadius) {
        // 两点在X轴的距离
        float lenX = (float) (touchPoint.x - centerPoint.x);
        // 两点在Y轴距离
        float lenY = (float) (touchPoint.y - centerPoint.y);
        // 两点距离
        float lenXY = (float) Math.sqrt((double) (lenX * lenX + lenY * lenY));
        // 计算弧度
        double radian = Math.acos(lenX / lenXY) * (touchPoint.y < centerPoint.y ? -1 : 1);
        // 计算角度
        double angle = radian2Angle(radian);
        if (lenXY + rockerRadius <= regionRadius) { // 触摸位置在可活动范围内
            // 回调 返回参数
            callBack(angle, (int) lenXY);
            return touchPoint;
        } else { // 触摸位置在可活动范围以外
            // 计算要显示的位置
            int showPointX = (int) (centerPoint.x + (regionRadius - rockerRadius) * Math.cos(radian));
            int showPointY = (int) (centerPoint.y + (regionRadius - rockerRadius) * Math.sin(radian));
            callBack(angle, (int) Math.sqrt((showPointX - centerPoint.x) * (showPointX - centerPoint.x) + (showPointY - centerPoint.y) * (showPointY - centerPoint.y)));
            return new Point(showPointX, showPointY);
        }
    }

    private void callBack(double angle, int distance) {
        String TAG = "lzs";
        if (Math.abs(distance - lastDistance) >= (baseDistance / mDistanceLevel)) {
            lastDistance = distance;
            if (mDistanceLevelListener != null) {
                int level = (distance / (baseDistance / mDistanceLevel));
                mDistanceLevelListener.onDistanceLevel(level);
            }
        }
        if (mAngleChangeListener != null) {
            mAngleChangeListener.angle(angle);
        }
        if (mShakeListener != null) {
//            if (mDirectionListener != null) {
            if (CallBackMode.CALL_BACK_MODE_MOVE == mCallBackMode) {//移动

                Log.e(TAG, "callBack: 移动");
                switch (mDirectionMode) {
                    case DIRECTION_2_HORIZONTAL:// 左右方向
                        Log.e(TAG, "callBack: 左右方向");
                        if (ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
                            // 右
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                            mDirectionListener.onRight();
                        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle) {
                            // 左
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                            mDirectionListener.onLeft();
                        }
                        break;
                    case DIRECTION_2_VERTICAL:// 上下方向
                        Log.e(TAG, "callBack: 上下方向");
                        if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle) {
                            // 下
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                            mDirectionListener.onDown();
                        } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
                            // 上
                            mShakeListener.direction(Direction.DIRECTION_UP);
                            mDirectionListener.onUp();
                        }
                        break;
                    case DIRECTION_4_ROTATE_0:// 四个方向
                        Log.e(TAG, "callBack: 四个方向");
                        if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle) {
                            // 右下
                            mShakeListener.direction(Direction.DIRECTION_DOWN_RIGHT);
                            mDirectionListener.onDownRight();
                        } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle) {
                            // 左下
                            mShakeListener.direction(Direction.DIRECTION_DOWN_LEFT);
                            mDirectionListener.onDownLeft();
                        } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle) {
                            // 左上
                            mShakeListener.direction(Direction.DIRECTION_UP_LEFT);
                            mDirectionListener.onaUpLeft();
                        } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle) {
                            // 右上
                            mShakeListener.direction(Direction.DIRECTION_UP_RIGHT);
                            mDirectionListener.onUpRight();
                        }
                        break;
                    case DIRECTION_4_ROTATE_45:// 四个方向 旋转45度
                        Log.e(TAG, "callBack: 四个方向 旋转45度");
                        if (ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) {
                            // 右
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                        } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle) {
                            // 下
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                        } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle) {
                            // 左
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                        } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle) {
                            // 上
                            mShakeListener.direction(Direction.DIRECTION_UP);
                        }
                        break;
                    case DIRECTION_8:// 八个方向
                        Log.e(TAG, "callBack: 八个方向");
                        if (ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) {
                            // 右
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                        } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle) {
                            // 右下
                            mShakeListener.direction(Direction.DIRECTION_DOWN_RIGHT);
                        } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle) {
                            // 下
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                        } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle) {
                            // 左下
                            mShakeListener.direction(Direction.DIRECTION_DOWN_LEFT);
                        } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle) {
                            // 左
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                        } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle) {
                            // 左上
                            mShakeListener.direction(Direction.DIRECTION_UP_LEFT);
                        } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle) {
                            // 上
                            mShakeListener.direction(Direction.DIRECTION_UP);
                        } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle) {
                            // 右上
                            mShakeListener.direction(Direction.DIRECTION_UP_RIGHT);
                        }
                        break;
                    default:
                        break;
                }
            } else if (CallBackMode.CALL_BACK_MODE_STATE_CHANGE == mCallBackMode) {//改变
                Log.e(TAG, "callBack: 改变");
                switch (mDirectionMode) {
                    case DIRECTION_2_HORIZONTAL:// 左右方向
                        Log.e(TAG, "callBack: 左右方向");
                        if ((ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                        }
                        break;
                    case DIRECTION_2_VERTICAL:// 上下方向
                        Log.e(TAG, "callBack: 上下方向");
                        if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN;
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                        } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP;
                            mShakeListener.direction(Direction.DIRECTION_UP);
                        }
                        break;
                    case DIRECTION_4_ROTATE_0:// 四个方向
                        Log.e(TAG, "callBack: 四个方向");
                        if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                            // 右下
                            tempDirection = Direction.DIRECTION_DOWN_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_DOWN_RIGHT);
                        } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                            // 左下
                            tempDirection = Direction.DIRECTION_DOWN_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_DOWN_LEFT);
                        } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                            // 左上
                            tempDirection = Direction.DIRECTION_UP_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_UP_LEFT);
                        } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                            // 右上
                            tempDirection = Direction.DIRECTION_UP_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_UP_RIGHT);
                        }
                        break;
                    case DIRECTION_4_ROTATE_45:// 四个方向 旋转45度
                        Log.e(TAG, "callBack: 四个方向 旋转45度");
                        if ((ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                        } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN;
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                        } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                        } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP;
                            mShakeListener.direction(Direction.DIRECTION_UP);
                        }
                        break;
                    case DIRECTION_8:// 八个方向
                        Log.e(TAG, "callBack: 八个方向");
                        if ((ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) && tempDirection != Direction.DIRECTION_RIGHT) {
                            // 右
                            tempDirection = Direction.DIRECTION_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_RIGHT);
                        } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle && tempDirection != Direction.DIRECTION_DOWN_RIGHT) {
                            // 右下
                            tempDirection = Direction.DIRECTION_DOWN_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_DOWN_RIGHT);
                        } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle && tempDirection != Direction.DIRECTION_DOWN) {
                            // 下
                            tempDirection = Direction.DIRECTION_DOWN;
                            mShakeListener.direction(Direction.DIRECTION_DOWN);
                        } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle && tempDirection != Direction.DIRECTION_DOWN_LEFT) {
                            // 左下
                            tempDirection = Direction.DIRECTION_DOWN_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_DOWN_LEFT);
                        } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle && tempDirection != Direction.DIRECTION_LEFT) {
                            // 左
                            tempDirection = Direction.DIRECTION_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_LEFT);
                        } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle && tempDirection != Direction.DIRECTION_UP_LEFT) {
                            // 左上
                            tempDirection = Direction.DIRECTION_UP_LEFT;
                            mShakeListener.direction(Direction.DIRECTION_UP_LEFT);
                        } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle && tempDirection != Direction.DIRECTION_UP) {
                            // 上
                            tempDirection = Direction.DIRECTION_UP;
                            mShakeListener.direction(Direction.DIRECTION_UP);
                        } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle && tempDirection != Direction.DIRECTION_UP_RIGHT) {
                            // 右上
                            tempDirection = Direction.DIRECTION_UP_RIGHT;
                            mShakeListener.direction(Direction.DIRECTION_UP_RIGHT);
                        }
                        break;
                    default:
                        break;
                }
            }
//            }
        }
    }

    /**
     * 弧度转角度
     *
     * @param radian 弧度
     * @return 角度[0, 360)
     */
    private double radian2Angle(double radian) {
        double tmp = Math.round(radian / Math.PI * 180);
        return tmp >= 0 ? tmp : 360 + tmp;
    }

    /**
     * 开启回调
     */
    private void callBackStart() {
        tempDirection = Direction.DIRECTION_CENTER;
        if (mAngleChangeListener != null) {
            mAngleChangeListener.onStart();
        }
        if (mShakeListener != null) {
            mShakeListener.onStart();
        }
    }

    /**
     * 结束回调
     */
    private void callBackFinish() {
        tempDirection = Direction.DIRECTION_CENTER;
        if (mAngleChangeListener != null) {
            mAngleChangeListener.onFinish();
        }
        if (mShakeListener != null) {
            mShakeListener.onFinish();
        }
    }

    /**
     * 回调模式
     *
     * @param mode 回调类型
     * @return CallBackMode
     */
    private CallBackMode getCallBackMode(int mode) {
        switch (mode) {
            case 0:
                return CallBackMode.CALL_BACK_MODE_MOVE;
            case 1:
                return CallBackMode.CALL_BACK_MODE_STATE_CHANGE;
        }
        return mCallBackMode;
    }

    /**
     * @param angleChangeListener 添加摇杆摇动角度的监听
     */
    public void setOnAngleChangeListener(OnAngleChangeListener angleChangeListener) {
        mAngleChangeListener = angleChangeListener;
    }

    /**
     * @param distanceLevelListener 添加摇动的距离变化监听
     */
    public void setOnDistanceLevelListener(OnDistanceLevelListener distanceLevelListener) {
        mDistanceLevelListener = distanceLevelListener;
    }

    /**
     * @param shakeListener 添加摇动的监听
     */
    public void setOnShakeListener(OnShakeListener shakeListener) {
        mShakeListener = shakeListener;
    }

    /**
     * @param directionListener 添加方向监听
     */
    public void setOnDirectionListener(OnDirectionListener directionListener) {
        mDirectionListener = directionListener;
    }

    /**
     * @param mode 设置回调模式
     */
    public void setCallBackMode(CallBackMode mode) {
        mCallBackMode = mode;
    }

    /**
     * @param mode 设置遥控杆方向模式
     */
    public void setDirectionMode(DirectionMode mode) {
        mDirectionMode = mode;
    }

    /**
     * Drawable 转 Bitmap
     *
     * @param drawable Drawable
     * @return Bitmap
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
}
