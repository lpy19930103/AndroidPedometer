package com.lipy.step.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;
import com.lipy.step.result.PedometerUpDateListener;
import com.lipy.step.utils.TimeUtil;

import java.util.List;

/**
 * 计步器实现类
 * Created by lipy on 2017/4/10 0010.
 */
public class PedometerCore implements SensorEventListener {

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

    private static int TAG_STEP = -1;

    private CountDownTimer mCountDownTimer;


    public PedometerCore() {
        mContext = BaseApplication.getInstances().getAppContext();
    }

    /**
     *  初始化传感器相关数据
     */
    public void initData() {
        CURRENT_STEP = 0;
        BaseApplication.getInstances().setDatabase(mContext);
        mPedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();

//        List<PedometerEntity> pedometerEntities = mPedometerEntityDao.loadAll();

//        if (pedometerEntities != null && pedometerEntities.size() > 0) {
//            PedometerEntity pedometerEntity = pedometerEntities.get(pedometerEntities.size() - 1);
////            pedometerEntity.setDate("2017-4-18");//test code
//            if (TimeUtil.IsYesterday(pedometerEntity.getDate())) {//昨天
//                if (!pedometerEntity.getPunchCard()) {//未打卡
//                    Log.e(TAG, "是昨天未打卡 删除");
//                    //如果前一天未打卡就清掉数据库 从新开始记步
//                    mPedometerEntityDao.deleteAll();
//                }
//            } else if (!TimeUtil.IsToday(pedometerEntity.getDate())) {//不是昨天 也不是今天
//                Log.e(TAG, "不是昨天 也不是今天 删除");
//                //如果是跨天的情况下 就清掉数据库 从新开始记步
//                mPedometerEntityDao.deleteAll();
//            }
//        }


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
//                countDown();
                if (CURRENT_TOTAL_STEPS == (int) event.values[0]) {
                    return;//华为手机在传感器回掉比较特殊，会重复回掉同一个数值多次。
                }
                CURRENT_TOTAL_STEPS = (int) event.values[0];

                Log.e(TAG, "**************************Date =" + TimeUtil.getStringDateShort());
                Log.e(TAG, "CURRENT_TOTAL_STEPS = " + CURRENT_TOTAL_STEPS);

                mPedometerEntities = mPedometerEntityDao.loadAll();

                if (mPedometerEntities != null && mPedometerEntities.size() > 0) {
                    PedometerEntity pedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 1);
                    String date = pedometerEntity.getDate();
//                    date = "2017-05-04";
                    if (!TextUtils.isEmpty(date)) {
                        if (TimeUtil.IsToday(date)) {
                            mTodayStepEntity = pedometerEntity;
                            Log.e(TAG, "还是今天 复用今天的Entity");
                        } else if (TimeUtil.IsYesterday(date)) {//跨天
                            if (pedometerEntity.getPunchCard()) {//打过卡 区分步数算入那一天
                                Log.e(TAG, "是昨天 创建新的Entity ");
                                if (CURRENT_TOTAL_STEPS > pedometerEntity.getTotalSteps()) {//没有关机
                                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, CURRENT_TOTAL_STEPS - 1, false, false);
                                } else {
                                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0,
                                            pedometerEntity.getTotalSteps(), TAG_STEP, CURRENT_TOTAL_STEPS - 1, false, false);

                                }
                                mPedometerEntityDao.insert(mTodayStepEntity);
                            } else {
//                            如果前一天未打卡就标记步数用于计算 从新开始记步

                                mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, CURRENT_TOTAL_STEPS - 1, false, false);
                                TAG_STEP = CURRENT_TOTAL_STEPS;
                                if (CURRENT_TOTAL_STEPS > 1) {
                                    TAG_STEP = CURRENT_TOTAL_STEPS - 1;
                                }
                                pedometerEntity.setTagStep(TAG_STEP);
                                mPedometerEntityDao.update(pedometerEntity);
                                mPedometerEntityDao.insert(mTodayStepEntity);
                                Log.e(TAG, "前一天未打卡");
                            }
                        } else {
                            //如果是跨天的情况下
                            mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getYesterday(), -1, CURRENT_TOTAL_STEPS - 1, TAG_STEP, 0, false, true));
                            mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, 0, false, false);
                            mPedometerEntityDao.insert(mTodayStepEntity);
                            Log.e(TAG, "跨天");

                        }
                    } else {
                        mPedometerEntityDao.deleteAll();
                        mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getYesterday(), -1, CURRENT_TOTAL_STEPS - 1, TAG_STEP, 0, false, true));
                        mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, 0, false, false);
                        mPedometerEntityDao.insert(mTodayStepEntity);
                        Log.e(TAG, "日期为null 脏数据");
                    }

                } else {
                    mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getYesterday(), -1, CURRENT_TOTAL_STEPS - 1, TAG_STEP, 0, false, true));
                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, CURRENT_TOTAL_STEPS, TAG_STEP, 0, false, true);
                    mPedometerEntityDao.insert(mTodayStepEntity);
                    Log.e(TAG, "第一次安装=  " + mPedometerEntityDao.loadAll().size());
                }

                mPedometerEntities = mPedometerEntityDao.loadAll();


                if (mPedometerEntities.size() > 1 && !TimeUtil.IsYesterday(mTodayStepEntity.getDate())) {
                    if (mYesterdayPedometerEntity == null || !TimeUtil.IsYesterday(mYesterdayPedometerEntity.getDate())) {
                        mYesterdayPedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 2);
                    }

                    if (CURRENT_TOTAL_STEPS <= 7 || mTodayStepEntity.getLastSystemSteps() > CURRENT_TOTAL_STEPS) {//重启
                        CURRENT_STEP = CURRENT_TOTAL_STEPS - LAST_SYSTEM_STEPS;
                        if (CURRENT_TOTAL_STEPS == LAST_SYSTEM_STEPS) {
                            CURRENT_STEP = -1;
                        }
                        mTodayStepEntity.setLastSystemSteps(0);
                        Log.d(TAG, "CURRENT_STEP1 = " + CURRENT_STEP);

                    } else {
                        CURRENT_STEP = CURRENT_TOTAL_STEPS - mTodayStepEntity.getLastSystemSteps();
                        if (CURRENT_TOTAL_STEPS == LAST_SYSTEM_STEPS) {
                            CURRENT_STEP = -1;
                        }
                        Log.d(TAG, "CURRENT_STEP2 = " + CURRENT_STEP);
                    }

                    //系统在每次手机静止不动一会时间 下次记步会记录7步
                    if (CURRENT_STEP == 7) {
                        CURRENT_STEP = 1;
                    }

                    Log.d(TAG, "CURRENT_TOTAL_STEPS = " + CURRENT_TOTAL_STEPS);
                    Log.d(TAG, "LAST_SYSTEM_STEPS = " + LAST_SYSTEM_STEPS);
                    Log.d(TAG, "LAST_SYSTEM_STEPS_IN_DAO = " + mTodayStepEntity.getLastSystemSteps());

                    if (!mYesterdayPedometerEntity.getPunchCard() && mYesterdayPedometerEntity.getTagStep() != -1) {

                        if (CURRENT_TOTAL_STEPS < mTodayStepEntity.getTotalSteps()) {
                            mTodayStepEntity.setReStart(true);
                            if (CURRENT_STEP == 0) {
                                CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();//纠正总步数
                            } else if (CURRENT_STEP == -1) {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps();
                            } else {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;//纠正总步数
                            }
                        } else if (mTodayStepEntity.getReStart()) {//当前是重启状态
                            if (CURRENT_STEP == 0) {
                                CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();//纠正总步数
                            } else if (CURRENT_STEP == -1) {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps();
                            } else {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;//纠正总步数
                            }
                        }
//                        else {
//                            //系统在每次手机静止不动一会时间 下次记步会记录7步
//                            if ((CURRENT_TOTAL_STEPS - LAST_SYSTEM_STEPS) == 7) {
//                                CURRENT_TOTAL_STEPS = CURRENT_TOTAL_STEPS - 6;
//                            }
//                        }
                        DAILY_STEP = CURRENT_TOTAL_STEPS - mYesterdayPedometerEntity.getTagStep();
                    } else {
//                        if (CURRENT_TOTAL_STEPS < mYesterdayPedometerEntity.getTotalSteps()) {//当前系统步数 < 昨日记录总步数 可以判断为重启手机

//                            CURRENT_TOTAL_STEPS += mYesterdayPedometerEntity.getTotalSteps();//纠正总步数

                        if (mTodayStepEntity.getTotalSteps() > CURRENT_TOTAL_STEPS) {
                            mTodayStepEntity.setReStart(true);
                            if (CURRENT_STEP == 0) {
                                CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();//纠正总步数
                            } else if (CURRENT_STEP == -1) {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps();
                            } else {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;//纠正总步数
                            }
//                            }
                        } else if (mTodayStepEntity.getReStart()) {//当前是重启状态

                            if (CURRENT_STEP == 0) {
                                CURRENT_TOTAL_STEPS += mTodayStepEntity.getTotalSteps();//纠正总步数
                            } else if (CURRENT_STEP == -1) {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps();
                            } else {
                                CURRENT_TOTAL_STEPS = mTodayStepEntity.getTotalSteps() + CURRENT_STEP;//纠正总步数
                            }
                        }
//                            else {
//                            //系统在每次手机静止不动一会时间 下次记步会记录7步
//                            if ((CURRENT_TOTAL_STEPS - LAST_SYSTEM_STEPS) == 7) {
//                                CURRENT_TOTAL_STEPS = CURRENT_TOTAL_STEPS - 6;
//                            }
//                        }
                        DAILY_STEP = CURRENT_TOTAL_STEPS - mYesterdayPedometerEntity.getTotalSteps();//正常记步
                    }

                    if (22 <= TimeUtil.getHour() && TimeUtil.getHour() <= 24) {
                        mTodayStepEntity.setPunchCard(true);
                    }

                    mTodayStepEntity.setTotalSteps(CURRENT_TOTAL_STEPS);
                    mTodayStepEntity.setDailyStep(DAILY_STEP);
                    Log.e(TAG, "当日步数 = " + mTodayStepEntity.getDailyStep());
                }
            }

            if (LAST_SYSTEM_STEPS != 0) {
                mTodayStepEntity.setLastSystemSteps((int) event.values[0]);
            }
            CURRENT_TOTAL_STEPS = LAST_SYSTEM_STEPS = (int) event.values[0];

            mPedometerEntityDao.update(mTodayStepEntity);

            if (mPedometerUpDateListener != null) {
                mPedometerUpDateListener.PedometerUpDate(mTodayStepEntity);
            }
            Log.e(TAG, "昨日总步数 " + mYesterdayPedometerEntity.getTotalSteps());
            Log.e(TAG, "目前总步数 " + mTodayStepEntity.getTotalSteps());
            Log.e(TAG, "统计天数 " + mPedometerEntityDao.loadAll().size());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void onDestroy() {
        CURRENT_STEP = 0;
        mTodayStepEntity = null;
    }

//    private void countDown() {
//        if (mCountDownTimer == null) {
//            mCountDownTimer = new CountDownTimer(300000, 1000) {
//                @Override
//                public void onTick(long l) {
//                    //倒计时每秒的回调
//                }
//
//                @Override
//                public void onFinish() {
//                    //倒计时结束
//                    if (mTodayStepEntity != null &&
//                            mTodayStepEntity.getDailyStep() >= DAILY_STEP &&
//                            mTodayStepEntity.getDailyStep() >= DAILY_STEP) {
//
//                        mTodayStepEntity.setTotalSteps(CURRENT_TOTAL_STEPS);
//                        mTodayStepEntity.setDailyStep(DAILY_STEP);
//
//                        Log.e(TAG, "无操作倒计时 每日步数= " + mTodayStepEntity.getDailyStep());
//                    }
//                }
//            };
//        } else {
//            mCountDownTimer.cancel();
//        }
//        mCountDownTimer.start();
//    }

}
