package com.lipy.step.receiver;

import com.lipy.step.pedometer.ActionModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 解锁
 * Created by lipy on 2017/4/18 0018.
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {

    private String action = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        Log.e("LIPY","action:="+action);
        if (Intent.ACTION_SCREEN_ON.equals(action) ||// 开屏
                Intent.ACTION_SCREEN_OFF.equals(action) ||// 锁屏
                Intent.ACTION_USER_PRESENT.equals(action)) { // 锁屏
            ActionModule.getInstance().getPedometerManager().startPedometerService();
        }
    }

    /**
     * 开始监听screen状态
     *
     */
//    public void begin() {
//        registerListener();
//        getScreenState();
//    }
//    /**
//     * 获取screen状态
//     */
//    private void getScreenState() {
//        PowerManager manager = (PowerManager) mContext
//                .getSystemService(Context.POWER_SERVICE);
//        if (manager.isScreenOn()) {
//            if (mScreenStateListener != null) {
//                mScreenStateListener.onScreenOn();
//            }
//        } else {
//            if (mScreenStateListener != null) {
//                mScreenStateListener.onScreenOff();
//            }
//        }
//    }
//    /**
//     * 停止screen状态监听
//     */
//    public void unregisterListener() {
//        mContext.unregisterReceiver(mScreenReceiver);
//    }
//    /**
//     * 启动screen状态广播接收器
//     */
//    private void registerListener() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_USER_PRESENT);
//        mContext.registerReceiver(mScreenReceiver, filter);
//    }
}
