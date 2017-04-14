package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.common.PedometerEvent;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;
import com.lipy.step.result.IGetPedometerResult;
import com.lipy.step.result.PedometerUpDateListener;
import com.lipy.step.utils.TimeUtil;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * 计步器实现类
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerRepositoryIml implements SensorEventListener, PedometerRepository {

    private Context mContext;

    private static int FIRST_STEP_COUNT = 0;    //每天打开软件记录的初始步数，用于带count传感器算法的方式
    private static int CURRENT_STEP = 0;        //当天的记步数总数

    private PedometerUpDateListener mPedometerUpDateListener;

    private PedometerEntity mTodayStepEntity;
    private static int TODAY_ENTITY_STEPS;
    private List<PedometerEntity> mPedometerEntities;
    private PedometerEntityDao mPedometerEntityDao;
    private PedometerEntity mYesterdayPedometerEntity;
    private int allStep = 0;
    private int next = 0;
    private boolean nextB = true;


    public PedometerRepositoryIml() {
        mContext = BaseApplication.getAppContext();
        Log.e("lipy", "mContext:" + mContext);
    }

    /**
     *  初始化传感器相关数据
     */
    public void initData() {
        FIRST_STEP_COUNT = 0;
        CURRENT_STEP = 0;
        TODAY_ENTITY_STEPS = 0;
    }


    public void setPedometerUpDateListener(PedometerUpDateListener upDateListener) {
        mPedometerUpDateListener = upDateListener;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor == null) {
            return;
        }

        synchronized (this) {

            if (mPedometerEntityDao == null) {
                BaseApplication.getInstances().setDatabase(mContext);
                mPedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
            }

            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                allStep = (int) event.values[0];
                CURRENT_STEP = allStep - next;

                Log.e("lipy", "**************************Date =" + TimeUtil.getStringDateShort());
                Log.e("lipy", "FIRST_STEP_COUNT = " + allStep);
                Log.e("lipy", "next = " + next);
                Log.e("lipy", "CURRENT_STEP = " + CURRENT_STEP);


                mPedometerEntities = mPedometerEntityDao.loadAll();
                if (mPedometerEntities != null && mPedometerEntities.size() > 0) {
                    PedometerEntity pedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 1);
                    String date = pedometerEntity.getDate();
                    if (!TextUtils.isEmpty(date)) {
                        if (TimeUtil.IsToday(date)) {
                            mTodayStepEntity = pedometerEntity;
                            Log.e("lipy", "还是今天 复用今天的Entity");
                        } else if (TimeUtil.IsYesterday(date) && mPedometerEntities.size() > 1) {
                            Log.e("lipy", "是昨天 创建新的Entity ");
                            if (allStep > mPedometerEntities.get(mPedometerEntities.size() - 2).getTotalSteps()) {
                                mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, allStep, 0, false);
                            } else {
                                mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, allStep +
                                        mPedometerEntities.get(mPedometerEntities.size() - 2).getTotalSteps(), 0, false);

                            }
                            mPedometerEntityDao.insert(mTodayStepEntity);
                        }
                    }

                } else {
                    mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, allStep, 0, false));
                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, allStep, 0, false);
                    mPedometerEntityDao.insert(mTodayStepEntity);
                    Log.e("lipy", "第一次安装创建2个Entity=  " + mPedometerEntityDao.loadAll().size());
                }

                mPedometerEntities = mPedometerEntityDao.loadAll();


                if (mPedometerEntities.size() > 1 && !TimeUtil.IsYesterday(mTodayStepEntity.getDate())) {
                    if (mYesterdayPedometerEntity == null) {
                        mYesterdayPedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 2);
                    }
                    int dailyStep;

                    if (allStep < mYesterdayPedometerEntity.getTotalSteps()) {//当前系统步数 < 昨日记录总步数 判断为重启手机
                        mTodayStepEntity.setRestart(true);

                        allStep += mYesterdayPedometerEntity.getTotalSteps();//纠正总步数
                        Log.e("lipy", "纠正总步数1 = " + allStep);

                        if (mTodayStepEntity.getTotalSteps() > allStep) {
                            if (next != 0 || CURRENT_STEP != 0) {
                                allStep = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;
                            } else {
                                allStep += mTodayStepEntity.getTotalSteps();
                            }

                            Log.e("lipy", "纠正总步数2 = " + allStep);
                        }
                    } else if (mTodayStepEntity.getRestart()) {//当前是重启状态

                        if (next != 0 || CURRENT_STEP != 0) {
                            allStep = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;
                        } else {
                            allStep += mTodayStepEntity.getTotalSteps();
                        }
                        Log.e("lipy", "关机状态 = " + mTodayStepEntity.getRestart());
                    }

                    dailyStep = allStep - mYesterdayPedometerEntity.getTotalSteps();//正常记步

                    mTodayStepEntity.setTotalSteps(allStep);
                    mTodayStepEntity.setDailyStep(dailyStep);
                    Log.e("lipy", "当日步数 = " + mTodayStepEntity.getDailyStep());
                }
            }

            mPedometerEntityDao.update(mTodayStepEntity);

            if (mPedometerUpDateListener != null) {
                mPedometerUpDateListener.PedometerUpDate(mTodayStepEntity);
            }
            Log.e("lipy", "昨日总步数 " + mYesterdayPedometerEntity.getTotalSteps());
            Log.e("lipy", "总步数 " + mTodayStepEntity.getTotalSteps());
            Log.e("lipy", "统计天数 " + mPedometerEntityDao.loadAll().size());
            next = (int) event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * 获取行走步数
     */
    private void showSteps(int steps) {
        if (steps < 0) {
            return;
        }
        steps = steps + TODAY_ENTITY_STEPS;

        mTodayStepEntity.setDailyStep(steps);

        PedometerEvent event = new PedometerEvent();
        event.mIsUpdate = true;
        BaseApplication.postEvent(event);

    }

    @Override
    public void getPedometerStep(IGetPedometerResult result) {

        result.onSuccessGet(mTodayStepEntity);
    }

    public void onDestroy() {
        mTodayStepEntity.setTagStep(0);
        mPedometerEntityDao.update(mTodayStepEntity);
        FIRST_STEP_COUNT = 0;
        CURRENT_STEP = 0;
        mTodayStepEntity = null;
    }
}
