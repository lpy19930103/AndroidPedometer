package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.result.PedometerUpDateListener;

import android.content.Context;


public class ApplicationModule {

    private final Context mAppContent;

    private static ApplicationModule sInstance;

    private PedometerManager mPedometerManager;// 计步器管理类:通知和定时器等
    private PedometerRepositoryIml mPedometerRepository;


    /**
     * 初始化单例,在程序启动时调用<br>
     */
    public static ApplicationModule initSingleton() {
        if (sInstance == null) {
            sInstance = new ApplicationModule();
        }

        return sInstance;
    }

    public static ApplicationModule getInstance() {
        return sInstance;
    }

    public void onCreateMainProcess() {
        mPedometerManager = new PedometerManager();
        mPedometerRepository = new PedometerRepositoryIml();

    }


    private ApplicationModule() {
        mAppContent = BaseApplication.getAppContext();
    }


    public PedometerManager getPedometerManager() {
        return mPedometerManager;
    }

    public PedometerRepositoryIml getPedometerRepository() {
        return mPedometerRepository;
    }

    public void setPedometerUpDateListener(PedometerUpDateListener pedometerUpDateListener) {
        if (mPedometerRepository != null) {
            mPedometerRepository.setPedometerUpDateListener(pedometerUpDateListener);
        }
    }


}
