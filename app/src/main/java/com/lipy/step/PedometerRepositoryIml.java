package com.lipy.step;

import com.lipy.step.dao.core.PedometerEntity;
import com.lipy.step.dao.core.PedometerEntityDao;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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


    /**
     * 加速度传感器计算相应参数
     */
    private static float SENSITIVITY = 5f;   //SENSITIVITY灵敏度
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static long end = 0;
    private static long start = 0;

    /**
     * 最后加速度方向
     */
    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2]};
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    private int mAccelerometerMode = 2;  //算法1 2


    private PedometerEntity mTodayStepEntity;
    private static int TODAY_ENTITY_STEPS;
    private List<PedometerEntity> mPedometerEntities;
    private PedometerEntityDao mPedometerEntityDao;


    public PedometerRepositoryIml() {
        mContext = BaseApplication.getAppContext();
    }

    /**
     *  初始化传感器相关数据
     */
    public void initData() {
        initAccData();

        FIRST_STEP_COUNT = 0;
        CURRENT_STEP = 0;
        TODAY_ENTITY_STEPS = 0;


    }

    private void initAccData() {
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor == null) {
            return;
        }

        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                mPedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
                mPedometerEntities = mPedometerEntityDao.loadAll();
                Log.i("lipy", "Date =" + TimeUtil.getStringDateShort());
                if (mPedometerEntities != null && mPedometerEntities.size() > 0) {
                    PedometerEntity pedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 1);
                    String date = pedometerEntity.getDate();
                    if (!TextUtils.isEmpty(date)) {
                        if (TimeUtil.IsToday(date)) {
                            mTodayStepEntity = pedometerEntity;
                        } else if (TimeUtil.IsYesterday(date)) {
                            mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, 0.0, 0, 0);
                            mPedometerEntityDao.insert(mTodayStepEntity);
                        }
                    }
                } else {
                    mPedometerEntityDao.insert(new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, 0.0, 1, (int) event.values[0]));
                    mTodayStepEntity = new PedometerEntity(null, TimeUtil.getStringDateShort(), 0, 0.0, 0, 0);
                    mPedometerEntityDao.insert(mTodayStepEntity);
                }

                if (TimeUtil.getStringDateShort().equals(mTodayStepEntity.getDate())) {
                    if (mPedometerEntities.size() > 1) {
                        if (!TimeUtil.IsYesterday(mTodayStepEntity.getDate())) {
                            PedometerEntity pedometerEntity = mPedometerEntities.get(mPedometerEntities.size() - 2);
                            mTodayStepEntity.setTargetStepCount((int) event.values[0] - pedometerEntity.getTargetStepCount());
                        }
                    }
                }else{

                }

                mPedometerEntityDao.update(mTodayStepEntity);

                Log.e("lipy", "TargetStepCount = " + mTodayStepEntity.getTargetStepCount());

                // Step Counter，要准确很多,读取开机的传感器步数
                Log.i("lipy", "FIRST_STEP_COUNT =" + FIRST_STEP_COUNT);

                if (FIRST_STEP_COUNT == 0) {

                    FIRST_STEP_COUNT = (int) event.values[0];

                    Log.i("lipy", "FIRST_STEP_COUNT =" + FIRST_STEP_COUNT);
                } else {
                    CURRENT_STEP = Math.abs((int) event.values[0] - FIRST_STEP_COUNT);
                    showSteps(CURRENT_STEP);
                    Log.i("lipy", "CURRENT_STEP = " + CURRENT_STEP);
                }
            } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {//加速度
                if (mAccelerometerMode == 1) {
                    //加速度取模
                    float vSum = 0;
                    for (int i = 0; i < 3; i++) {
                        final float v = mYOffset + event.values[i] * mScale[1];
                        vSum += v;
                    }
                    int k = 0;
                    float v = vSum / 3;

                    float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                    if (direction == -mLastDirections[k]) {
                        // Direction changed
                        int extType = (direction > 0 ? 0 : 1); // minumum or
                        // maximum?
                        mLastExtremes[extType][k] = mLastValues[k];
                        float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                        if (diff > SENSITIVITY) {
                            boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                            boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                            boolean isNotContra = (mLastMatch != 1 - extType);

                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                end = System.currentTimeMillis();

                                //步数矫正，步伐间隔<200ms和>2000ms，认为是无效步数,这部分也是目前终端计步器算法的核心。
                                if (end - start > 200 && end - start < 2000) {  // 此时判断为走了一步
                                    CURRENT_STEP++;
                                    mLastMatch = extType;
                                    showSteps(CURRENT_STEP);
                                }
                                start = end;
                            } else {
                                mLastMatch = -1;
                            }
                        }
                        mLastDiff[k] = diff;
                    }
                    mLastDirections[k] = direction;
                    mLastValues[k] = v;
                } else if (mAccelerometerMode == 2) {
                    for (int i = 0; i < 3; i++) {
                        oriValues[i] = event.values[i];
                    }
                    gravityNew = (float) Math.sqrt(oriValues[0] * oriValues[0]
                            + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]);
                    DetectorNewStep(gravityNew);
                }

            }
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

        Log.i("lipy", "<PedometerRepositoryIml> showSteps TODAY_ENTITY_STEPS= " + TODAY_ENTITY_STEPS + " steps = " + steps);

        mTodayStepEntity.setStepCount(steps);

        PedometerEvent event = new PedometerEvent();
        event.mIsUpdate = true;
        BaseApplication.postEvent(event);

    }

    @Override
    public void getPedometerStep(IGetPedometerResult result) {

        result.onSuccessGet(mTodayStepEntity);
    }

    public void onDestroy() {
        FIRST_STEP_COUNT = 0;
        mTodayStepEntity = null;
    }


    //存放三轴数据
    float[] oriValues = new float[3];
    final int valueNum = 4;
    //用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[valueNum];
    int tempCount = 0;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降 boolean lastStatus = false;
    // 波峰值
    float peakOfWave = 0;
    // 波谷值
    float valleyOfWave = 0; //此次波峰的时间
    long timeOfThisPeak = 0; //上次波峰的时间
    long timeOfLastPeak = 0; //当前的时间
    long timeOfNow = 0; //当前传感器的值
    float gravityNew = 0; //上次传感器的值
    float gravityOld = 0; //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float initialValue = 5.3f; //初始阈值
    float ThreadValue = 2.0f;
    boolean lastStatus = true;

    /*
    * 检测步子，并开始计步
    * 1.传入sersor中的数据
    * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
    * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
    * */
    public void DetectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();
                if (timeOfNow - timeOfLastPeak >= 250
                        && (peakOfWave - valleyOfWave >= ThreadValue)) {
                    timeOfThisPeak = timeOfNow;
                /*
                * 更新界面的处理，不涉及到算法
                * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
                * 1.连续记录10才开始计步
                * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
                * 3.连续记录了9步用户还在运动，之前的数据才有效
                * */
                    Log.i("lipy", "<PedometerRepositoryIml> DetectorNewStep CURRENT_STEP= " + CURRENT_STEP);

                    CURRENT_STEP++;
                    showSteps(CURRENT_STEP);
                }
                if (timeOfNow - timeOfLastPeak >= 250
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

    /*
    * 检测波峰
    * 以下四个条件判断为波峰：
    * 1.目前点为下降的趋势：isDirectionUp为false
    * 2.之前的点为上升的趋势：lastStatus为true
    * 3.到波峰为止，持续上升大于等于2次
    * 4.波峰值大于20
    * 记录波谷值 * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值 * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
    * */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 || oldValue >= 20)) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /*
    * 阈值的计算
    * 1.通过波峰波谷的差值计算阈值
    * 2.记录4个值，存入tempValue[]数组中
    * 3.在将数组传入函数averageValue中计算阈值
    * */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }

    /*
    * 梯度化阈值
    * 1.计算数组的均值
    * 2.通过均值将阈值梯度化在一个范围里
    * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8)
            ave = (float) 4.3;
        else if (ave >= 7 && ave < 8)
            ave = (float) 3.3;
        else if (ave >= 4 && ave < 7)
            ave = (float) 2.3;
        else if (ave >= 3 && ave < 4)
            ave = (float) 2.0;
        else {
            ave = (float) 1.3;
        }
        return ave;
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */


}
