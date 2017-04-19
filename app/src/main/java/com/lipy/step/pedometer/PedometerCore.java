package com.lipy.step.pedometer;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;
import com.lipy.step.result.IGetPedometerResult;
import com.lipy.step.result.PedometerUpDateListener;
import com.lipy.step.utils.TimeUtil;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * 计步器实现类
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerCore implements SensorEventListener, PedometerRepository {

    private static String TAG = "PedometerCore";

    private Context mContext;

    private static int CURRENT_STEP = 0;//当前步数差

    private static int CURRENT_TOTAL_STEPS = 0;//当天的记步数总数

    private static int LAST_SYSTEM_STEPS = 0;//上一次记步数

    private static int DAILY_STEP;//今日步数

    private PedometerUpDateListener mPedometerUpDateListener;

    private PedometerEntity mTodayStepEntity;

    private List<PedometerEntity> mPedometerEntities;

    private PedometerEntityDao mPedometerEntityDao;

    private PedometerEntity mYesterdayPedometerEntity;

    private static int TAG_STEP = 0;

    private CountDownTimer mCountDownTimer;


    public PedometerCore() {
        mContext = BaseApplication.getInstances().getAppContext();
    }

    /**
     *  初始化传感器相关数据
     */
    public void initData(int tagStep) {
        Log.e(TAG, "initData tagStep = " + tagStep);
        TAG_STEP = tagStep;
        CURRENT_STEP = 0;
        BaseApplication.getInstances().setDatabase(mContext);
        mPedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
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

                //倒计时
                countDown();

                CURRENT_TOTAL_STEPS = (int) event.values[0];
                CURRENT_STEP = CURRENT_TOTAL_STEPS - LAST_SYSTEM_STEPS;

                Log.e(TAG, "**************************Date =" + TimeUtil.getStringDateShort());
                Log.e(TAG, "CURRENT_TOTAL_STEPS = " + CURRENT_TOTAL_STEPS);
                Log.e(TAG, "LAST_SYSTEM_STEPS = " + LAST_SYSTEM_STEPS);
                Log.e(TAG, "CURRENT_STEP = " + CURRENT_STEP);

                mPedometerEntities = mPedometerEntityDao.loadAll();

                if (mPedometerEntities != null && mPedometerEntities.size() > 0) {
                    PedometerEntity pedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 1);
                    String date = pedometerEntity.getDate();
                    if (!TextUtils.isEmpty(date)) {
                        if (TimeUtil.IsToday(date)) {
                            mTodayStepEntity = pedometerEntity;
                            Log.e(TAG, "还是今天 复用今天的Entity");
                        } else if (TimeUtil.IsYesterday(date) && mPedometerEntities.size() > 1) {//跨天
                            PedometerEntity pedometerEntity1 = mPedometerEntities.get(mPedometerEntities.size() - 2);
                            if (pedometerEntity1.getPunchCard()) {//打过卡
                                Log.e(TAG, "是昨天 创建新的Entity ");
                                if (CURRENT_TOTAL_STEPS > pedometerEntity1.getTotalSteps()) {//没有关机
                                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false);
                                } else {
                                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS +
                                            pedometerEntity1.getTotalSteps(), TAG_STEP, false, false);

                                }
                                mPedometerEntityDao.insert(mTodayStepEntity);
                            } else {
                                //如果前一天未打卡就清掉数据库 从新开始记步
                                mPedometerEntityDao.deleteAll();
                                mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false));
                                mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false);
                                mPedometerEntityDao.insert(mTodayStepEntity);
                                Log.e(TAG, "前一天未打卡 创建2个Entity=  " + mPedometerEntityDao.loadAll().size());
                            }
                        } else if (mPedometerEntities.size() > 1) {

                            //如果是跨天的情况下 就清掉数据库 从新开始记步
                            mPedometerEntityDao.deleteAll();
                            mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false));
                            mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false);
                            mPedometerEntityDao.insert(mTodayStepEntity);
                            Log.e(TAG, "跨天 未计数 创建2个Entity=  " + mPedometerEntityDao.loadAll().size());

                        }
                    }

                } else {
                    mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false));
                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, false, false);
                    mPedometerEntityDao.insert(mTodayStepEntity);
                    Log.e(TAG, "第一次安装创建2个Entity=  " + mPedometerEntityDao.loadAll().size());
                }

                mPedometerEntities = mPedometerEntityDao.loadAll();


                if (mPedometerEntities.size() > 1 && !TimeUtil.IsYesterday(mTodayStepEntity.getDate())) {
                    if (mYesterdayPedometerEntity == null) {
                        mYesterdayPedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 2);
                    }

                    if (CURRENT_TOTAL_STEPS < mYesterdayPedometerEntity.getTotalSteps()) {//当前系统步数 < 昨日记录总步数 判断为重启手机
                        mTodayStepEntity.setReStart(true);

                        CURRENT_TOTAL_STEPS += mYesterdayPedometerEntity.getTotalSteps();//纠正总步数
                        Log.e(TAG, "纠正总步数1 = " + CURRENT_TOTAL_STEPS);

                        if (mTodayStepEntity.getTotalSteps() > CURRENT_TOTAL_STEPS) {
                            if (LAST_SYSTEM_STEPS != 0 || CURRENT_STEP != 0) {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;//纠正总步数
                            } else {
                                CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();//纠正总步数
                            }
                            Log.e(TAG, "纠正总步数2 = " + CURRENT_TOTAL_STEPS);
                        }
                    } else if (mTodayStepEntity.getReStart()) {//当前是重启状态

                        if (LAST_SYSTEM_STEPS != 0 || CURRENT_STEP != 0) {
                            CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;
                        } else {
                            CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();
                        }
                        Log.e(TAG, "关机状态 = " + mTodayStepEntity.getReStart());
                    }

                    DAILY_STEP = CURRENT_TOTAL_STEPS - mYesterdayPedometerEntity.getTotalSteps();//正常记步

                    if (22 <= TimeUtil.getHour() && TimeUtil.getHour() <= 24) {
                        mTodayStepEntity.setPunchCard(true);
                    }

                    mTodayStepEntity.setTotalSteps(CURRENT_TOTAL_STEPS);
                    mTodayStepEntity.setDailyStep(DAILY_STEP);
                    Log.e(TAG, "当日步数 = " + mTodayStepEntity.getDailyStep());
                }
            }

            mPedometerEntityDao.update(mTodayStepEntity);

            if (mPedometerUpDateListener != null) {
                mPedometerUpDateListener.PedometerUpDate(mTodayStepEntity);
            }
            Log.e(TAG, "昨日总步数 " + mYesterdayPedometerEntity.getTotalSteps());
            Log.e(TAG, "目前总步数 " + mTodayStepEntity.getTotalSteps());
            Log.e(TAG, "统计天数 " + mPedometerEntityDao.loadAll().size());
            LAST_SYSTEM_STEPS = (int) event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void getPedometerStep(IGetPedometerResult result) {

        result.onSuccessGet(mTodayStepEntity);
    }

    public void onDestroy() {
        CURRENT_STEP = 0;
        mTodayStepEntity = null;
    }

    private void countDown() {
        if (mCountDownTimer == null) {
            mCountDownTimer = new CountDownTimer(300000, 1000) {
                @Override
                public void onTick(long l) {
                    //倒计时每秒的回调
                }

                @Override
                public void onFinish() {
                    //倒计时结束
                    if (mTodayStepEntity != null &&
                            mTodayStepEntity.getDailyStep() >= DAILY_STEP &&
                            mTodayStepEntity.getDailyStep() >= DAILY_STEP) {

                        mTodayStepEntity.setTotalSteps(CURRENT_TOTAL_STEPS);
                        mTodayStepEntity.setDailyStep(DAILY_STEP);
                        Log.e(TAG, "无操作倒计时 每日步数= " + mTodayStepEntity.getDailyStep());
                    }
                }
            };
        } else {
            mCountDownTimer.cancel();
        }
        mCountDownTimer.start();
    }
}
