package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;

import android.content.Context;


public class ActionModule {

    private final Context mAppContent;

    private static ActionModule sInstance;

    private PedometerManager mPedometerManager;// 计步器管理类:通知和定时器等
    private PedometerCore mPedometerRepository;

    private ActionModule() {
        mAppContent = BaseApplication.getInstances().getAppContext();
    }

    public static ActionModule getInstance() {
        if (sInstance == null) {
            sInstance = new ActionModule();
        }
        return sInstance;
    }

    public void onCreateMainProcess() {
        mPedometerManager = new PedometerManager();
        mPedometerRepository = new PedometerCore();

    }


    public PedometerManager getPedometerManager() {
        return mPedometerManager;
    }

    public PedometerCore getPedometerRepository() {
        return mPedometerRepository;
    }

}
