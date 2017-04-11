package com.lipy.step;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

/**
 * 计步器硬件传感器工具类
 * Created by lipy on 2017/4/10 0010.
 */
public class HardwarePedometerUtil {

//    public static List<String> mStepCounterWhiteList = Arrays.asList(new String[]{"nexus5", "nexus6", "mx4", "mx5"});

    private static boolean areSensorsPresent(Context context) {
        return !((SensorManager) context.getSystemService(context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_STEP_COUNTER).isEmpty();
    }

//    /**
//     * step counter白名单列表
//     */
//    public static boolean isThisDeviceInStepCounterWhiteList() {
//        return mStepCounterWhiteList.contains(Build.MODEL.toLowerCase().replace(" ", ""));
//    }

    /**
     * 是否支持step counter记步传感器
     * 方式：1、有TYPE_STEP_COUNTER；2、版本为4.4(19)以上
     */
    public static boolean supportsHardwareStepCounter(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        Log.i("lipy", "sensor.getType() mStepCount = \n " + mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                + "\n mStepDetector = " + mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                + "\n mAccelerometer = " + mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

        return (Build.VERSION.SDK_INT >= 19) && (areSensorsPresent(context));
    }

    /**
     * 是否支持accelerometer传感器
     */
    public static boolean supportsHardwareAccelerometer(Context context) {
        return !((SensorManager) context.getSystemService(context.SENSOR_SERVICE)).
                getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty();
    }

    /**
     * Service的检查与启动:是否正在运行
     */
    public static boolean checkServiceRunning(Context context) {
        boolean isServiceRunning = false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.lipy.step.PedometerService".equals(service.service.getClassName())) {
                isServiceRunning = true;
            }
        }

        return isServiceRunning;
    }

}
