package com.bt.zhangzy.toolbox;

import android.app.Activity;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.bt.zhangzy.toolbox.manager.SensorZ;
import com.bt.zhangzy.tools.CompassView;


public class MainActivity extends Activity {

    private final float MAX_ROATE_DEGREE = 1.0f;// 最多旋转一周，即360°
    CompassView mPointer;

    SensorZ sensor;
    private SensorZ.SensorListener listener = new SensorZ.SensorListener() {
        @Override
        public void updateLocation(Location location) {

        }

        @Override
        public void setTargetDirection(float targetDirection) {
            mTargetDirection = targetDirection;
        }
    };
    private float mDirection;// 当前浮点方向
    private float mTargetDirection;// 目标浮点方向
    private AccelerateInterpolator mInterpolator;// 动画从开始到结束，变化率是一个加速的过程,就是一个动画速率
    protected final Handler mHandler = new Handler();
    private boolean mStopDrawing;// 是否停止指南针旋转的标志位
    // 这个是更新指南针旋转的线程，handler的灵活使用，每20毫秒检测方向变化值，对应更新指南针旋转
    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (mPointer != null && !mStopDrawing) {
                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE
                                : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = SensorZ.normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator
                            .getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
                                    : 0.3f)));// 用了一个加速动画去旋转图片，很细致
                    mPointer.updateDirection(mDirection);// 更新指南针旋转
                }

//                updateDirection();// 更新方向值

                mHandler.postDelayed(mCompassViewUpdater, 20);// 20毫米后重新执行自己，比定时器好
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensor = new SensorZ(this);
        sensor.regListener(listener);

        mPointer = (CompassView) findViewById(R.id.compass);
        light = (ImageView) findViewById(R.id.light);

        mDirection = 0.0f;// 初始化起始方向
        mTargetDirection = 0.0f;// 初始化目标方向
        mInterpolator = new AccelerateInterpolator();// 实例化加速动画对象
        mStopDrawing = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensor.onPause();
        mStopDrawing = true;
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor.onResume();
        mStopDrawing = false;
        mHandler.postDelayed(mCompassViewUpdater, 20);// 20毫秒执行一次更新指南针图片旋转
        boolean isOpen = getLightStatus(false);
        findViewById(R.id.light_btn).setSelected(isOpen);
        light.setImageResource(isOpen ? R.drawable.light_on : R.drawable.light_off);
    }


    //    private boolean lightOpen;
    Camera camera;
    ImageView light;

    public void onClickOpen(View view) {
        boolean isOpen = getLightStatus(true);
        view.setSelected(isOpen);
        light.setImageResource(isOpen ? R.drawable.light_on : R.drawable.light_off);
    }

    private boolean getLightStatus(boolean isChange) {
        if (camera == null) {
            camera = Camera.open();
        }
        Camera.Parameters mParameters = camera.getParameters();
        boolean isOpen = mParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF);
        if (isChange) {
            mParameters.setFlashMode(isOpen ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(mParameters);
        }
        return isChange ? isOpen : !isOpen;
    }


}
