package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.result.PedometerUpDateListener;
import com.lipy.step.utils.HardwarePedometerUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * 计步器服务，启动条件：
 * 1、有TYPE_STEP_COUNTER； 2、版本为4.4(19)以上
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerService extends Service {

    private static String TAG = "PedometerService";

    private Context mContext;

    private SensorManager mSensorManager;  // 传感器服务

    private PedometerCore mPedometerCore;  // 传感器监听对象

    private static final int RECEIVE_MESSAGE_CODE = 0x0001;

    private static final int SEND_MESSAGE_CODE = 0x0002;

    private Messenger clientMessenger = null;

    private PedometerEntity mPedometerEntity;


    //实现一个能够处理接收信息的Handler
    private Handler serviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RECEIVE_MESSAGE_CODE) {
                Log.i(TAG, "PedometerService serviceHandler");
                if (clientMessenger == null) {
                    clientMessenger = msg.replyTo;//这个Message是在客户端中创建的
                }
                if (clientMessenger != null && mPedometerEntity != null) {
                    Message msgToClient = Message.obtain();
                    msgToClient.what = SEND_MESSAGE_CODE;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("msg", mPedometerEntity);
                    msgToClient.setData(bundle);
                    try {
                        clientMessenger.send(msgToClient);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    //


    @Override
    public IBinder onBind(Intent intent) {
        Messenger serviceMessenger = new Messenger(serviceHandler);
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = BaseApplication.getAppContext();
        Log.i(TAG, "PedometerService onCreate");
        mPedometerCore = ApplicationModule.getInstance().getPedometerRepository();
        mPedometerCore.initData();
        mPedometerCore.setPedometerUpDateListener(new PedometerUpDateListener() {
            @Override
            public void PedometerUpDate(PedometerEntity pedometerEntity) {
                mPedometerEntity = pedometerEntity;
//                Log.e(TAG, "PedometerUpDate= " + pedometerEntity.getTargetStepCount());
                serviceHandler.sendEmptyMessage(RECEIVE_MESSAGE_CODE);
            }
        });

        ApplicationModule.getInstance().getPedometerManager().init();

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        if (HardwarePedometerUtil.supportsHardwareStepCounter(mContext)) {
            mSensorManager.registerListener(mPedometerCore, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_UI);
        } else if (HardwarePedometerUtil.supportsHardwareAccelerometer(mContext)) {
            mSensorManager.registerListener(mPedometerCore, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "PedometerService onStartCommand");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "PedometerService onDestroy");
        if (mPedometerCore != null) {
            mPedometerCore.onDestroy();
            mSensorManager.unregisterListener(mPedometerCore);
            mPedometerCore = null;
        }
        mSensorManager = null;
        mContext = null;
    }

}
