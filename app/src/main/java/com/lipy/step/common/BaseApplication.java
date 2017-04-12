package com.lipy.step.common;

import com.lipy.step.dao.DaoMaster;
import com.lipy.step.dao.DaoSession;
import com.lipy.step.pedometer.ApplicationModule;

import org.greenrobot.eventbus.EventBus;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


/**
 * application
 */
public class BaseApplication extends Application {

    protected static BaseApplication sInstance;

    protected final static EventBus GLOBAL_EVENT_BUS = EventBus.getDefault();
    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static BaseApplication instances;

    public BaseApplication() {
        sInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationModule.initSingleton().onCreateMainProcess();
        setDatabase(getAppContext());
    }

    public static Context getAppContext() {
        return sInstance;
    }

    public static BaseApplication getInstances() {
        if (instances == null) {
            instances = new BaseApplication();
        }
        return instances;
    }

    /**
     * 获取一个全局的EventBus实例<br>
     */
    public static EventBus getGlobalEventBus() {
        return GLOBAL_EVENT_BUS;
    }

    /**
     * 使用全局EventBus post一个事件<br>
     */
    public static void postEvent(Object event) {
        GLOBAL_EVENT_BUS.post(event);
    }

    /**
     * 使用全局EventBus post一个Sticky事件<br>
     */
    public static void postStickyEvent(Object event) {
        GLOBAL_EVENT_BUS.postSticky(event);
    }

    /**
     * 注册事件
     */
    public static void globalRegisterEvent(Object object) {
        GLOBAL_EVENT_BUS.register(object);
    }

    /**
     * 反注册事件
     */
    public static void globalUnRegisterEvent(Object object) {
        GLOBAL_EVENT_BUS.unregister(object);
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
