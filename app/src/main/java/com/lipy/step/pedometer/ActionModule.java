package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;

import android.content.Context;


public class ActionModule {

    private final Context mAppContent;

    private static ActionModule sInstance;

    private PedometerManager mPedometerManager;// 计步器管理类:通知和定时器等
    private PedometerCore mPedometerRepository;


    /**
     * 初始化单例,在程序启动时调用<br>
     */
    public static ActionModule initSingleton() {
        if (sInstance == null) {
            sInstance = new ActionModule();
        }
        return sInstance;
    }


    public static ActionModule getInstance() {
        return sInstance;
    }

    public void onCreateMainProcess() {
        mPedometerManager = new PedometerManager();
        mPedometerRepository = new PedometerCore();

    }


    private ActionModule() {
        mAppContent = BaseApplication.getInstances().getAppContext();
    }


    public PedometerManager getPedometerManager() {
        return mPedometerManager;
    }

    public PedometerCore getPedometerRepository() {
        return mPedometerRepository;
    }

}
