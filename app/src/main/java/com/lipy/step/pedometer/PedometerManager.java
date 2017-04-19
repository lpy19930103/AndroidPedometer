package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.common.Constants;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.result.PedometerUpDateResult;
import com.lipy.step.utils.HardwarePedometerUtil;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * 计步器管理类：1、记步服务管理
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerManager {
    public boolean IsServiceRunning = false;

    private Context mContext;

    private Intent mService;

    private static String PACKAGE_SERVICE = "com.lipy.step.pedometer";

    private static String NAME_SERVICE = "PedometerService";

    private static final int SEND_MESSAGE_CODE = 0x0001;

    private static final int RECEIVE_MESSAGE_CODE = 0x0002;

    private PedometerUpDateResult mPedometerUpDateResult;

    private Messenger serviceMessenger = null;

    private Messenger clientMessenger = new Messenger(new ClientHandler());
    private boolean mBindService;


    public PedometerManager() {
        mContext = BaseApplication.getInstances().getAppContext();
        mService = new Intent(mContext, PedometerService.class);
//        ComponentName componentName = new ComponentName(
//                PACKAGE_SERVICE,
//                NAME_SERVICE);
//        mService.setComponent(componentName);
    }

    /**
     * 服务启动后初始化
     */
    public void init() {

    }


    /**
     * Service的检查与启动:1、登录并且卡片存在
     * 1、有TYPE_STEP_COUNTER； 2、版本为4.4(19)以上
     */
    public void startPedometerService() {
        mContext.startService(mService);
        mBindService = mContext.bindService(mService, mServiceConnection, Context.BIND_AUTO_CREATE);
        IsServiceRunning = true;
    }

    /**
     * 1、停止记步服务；
     */
    public void stopPedometerService() {
        IsServiceRunning = false;
        mContext.unbindService(mServiceConnection);
        mContext.stopService(mService);
    }

    public void unbindPedometerService() {
        Log.e("lipy", "unbindPedometerService = " + mBindService);
        if (mBindService) {
            mContext.unbindService(mServiceConnection);
            mBindService = false;
        }
    }


    /**
     * Service的检查与启动:登录并且卡片存在
     */
    public void checkServiceStart() {

        if (HardwarePedometerUtil.supportsHardwareStepCounter(mContext)) {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.lipy.step.pedometer.PedometerService".equals(service.service.getClassName())) {
                    IsServiceRunning = true;

                }
            }
            Log.i("lipy", "checkServiceStart mIsServiceRunning = " + IsServiceRunning);


            if (IsServiceRunning) {
                mBindService = mContext.bindService(mService, mServiceConnection, Context.BIND_AUTO_CREATE);
            } else {
                startPedometerService();

            }
        }

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("lipy", "service connected");
            serviceMessenger = new Messenger(service);
            Message msg = Message.obtain();
            msg.what = SEND_MESSAGE_CODE;
            Bundle data = new Bundle();
            data.putInt("TAG_STEP", Constants.TAG_STEP);
            msg.setData(data);
            msg.replyTo = clientMessenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RECEIVE_MESSAGE_CODE) {
                Bundle data = msg.getData();
                if (data != null) {
                    PedometerEntity entity = (PedometerEntity) data.getSerializable("msg");
                    if (mPedometerUpDateResult != null) {
                        mPedometerUpDateResult.onPedometerUpDate(entity);
                    }
                }
            }
        }
    }

    public void setPedometerUpDateResult(PedometerUpDateResult upDateResult) {
        mPedometerUpDateResult = upDateResult;
    }
}
