package com.bt.zhangzy.toolbox.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by ZhangZy on 2015-11-4.
 */
public class SensorZ {

    public interface SensorListener{
        void updateLocation(Location location);
        void setTargetDirection(float targetDirection);
    }

    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;
    private LocationManager mLocationManager;
    private String mLocationProvider;
    private SensorListener listener;

    public SensorZ(Context context){
        initServices(context);
    }

    public void regListener(SensorListener listener){
        this.listener = listener;
    }

    // 初始化传感器和位置服务
    public void initServices(Context context) {
        // sensor manager
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION);

        // location manager
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();// 条件对象，即指定条件过滤获得LocationProvider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 较高精度
        criteria.setAltitudeRequired(false);// 是否需要高度信息
        criteria.setBearingRequired(false);// 是否需要方向信息
        criteria.setCostAllowed(true);// 是否产生费用
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 设置低电耗
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);// 获取条件最好的Provider

    }

    public void onPause(){
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(mOrientationSensorEventListener);
        }
        if (mLocationProvider != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    public void onResume(){
        if (mLocationProvider != null) {
            if(listener != null) {
                listener.updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            }
            mLocationManager.requestLocationUpdates(mLocationProvider, 2000,
                    10, mLocationListener);// 2秒或者距离变化10米时更新一次地理位置
        } else {
//            mLocationTextView.setText(R.string.cannot_get_location);
        }
        if (mOrientationSensor != null) {
            mSensorManager.registerListener(mOrientationSensorEventListener,
                    mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
//            Toast.makeText(this, R.string.cannot_get_sensor, Toast.LENGTH_SHORT).show();
        }
    }


    // 把经纬度转换成度分秒显示
    private String getLocationString(double input) {
        int du = (int) input;
        int fen = (((int) ((input - du) * 3600))) / 60;
        int miao = (((int) ((input - du) * 3600))) % 60;
        return String.valueOf(du) + "°" + String.valueOf(fen) + "′"
                + String.valueOf(miao) + "″";
    }

    // 方向传感器变化监听
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[0] * -1.0f;
            float mTargetDirection = normalizeDegree(direction);// 赋值给全局变量，让指南针旋转
            // 方向传递
            if(listener != null){
                listener.setTargetDirection(mTargetDirection);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // 调整方向传感器获取的值
    public final static float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    // 位置信息更新监听
    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status != LocationProvider.OUT_OF_SERVICE) {
                if(listener != null)
                    listener.updateLocation(mLocationManager
                        .getLastKnownLocation(mLocationProvider));
            } else {
//                mLocationTextView.setText(R.string.cannot_get_location);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            if(listener != null)
                listener.updateLocation(location);// 更新位置
        }
    };
}
