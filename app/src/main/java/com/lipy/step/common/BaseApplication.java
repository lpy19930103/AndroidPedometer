package com.lipy.step.common;

import com.lipy.step.dao.DaoMaster;
import com.lipy.step.dao.DaoSession;
import com.lipy.step.pedometer.ActionModule;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


/**
 * application
 */
public class BaseApplication extends Application {


    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static BaseApplication instances;

    public BaseApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instances = this;
        setDatabase(this);
        ActionModule.getInstance().onCreateMainProcess();
    }

    public Context getAppContext() {
        return instances.getApplicationContext();
    }

    public static BaseApplication getInstances() {
        return instances;
    }

    /**
     * 设置greenDao
     */
    public void setDatabase(Context context) {
        mHelper = new DaoMaster.DevOpenHelper(context, "pedometer-db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }


    public SQLiteDatabase getDb() {
        return db;
    }




}
