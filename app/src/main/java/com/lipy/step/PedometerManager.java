package com.lipy.step;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 计步器管理类：1、记步服务管理
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerManager {

    public boolean IsServiceRunning = false;

    private Context mContext;

    private Intent mService;


    public PedometerManager() {
        mContext = BaseApplication.getAppContext();
        mService = new Intent(mContext, PedometerService.class);
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
        IsServiceRunning = true;
    }

    /**
     * 1、停止记步服务；2、删除当天数据
     */
    public void stopPedometerService() {
        IsServiceRunning = false;
        mContext.stopService(mService);
    }


    /**
     * Service的检查与启动:登录并且卡片存在
     */
    public void checkServiceStart() {

        if (HardwarePedometerUtil.supportsHardwareStepCounter(mContext)) {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.lipy.step.PedometerService".equals(service.service.getClassName())) {
                    IsServiceRunning = true;
                }
            }

            Log.i("lipy", "<PedometerManager> checkServiceStart mIsServiceRunning = " + IsServiceRunning);

            if (!IsServiceRunning) {
                startPedometerService();
            }
        }

    }

}
