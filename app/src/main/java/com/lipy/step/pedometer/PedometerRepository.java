package com.lipy.step.pedometer;

import com.lipy.step.result.IGetPedometerResult;

/**
 * Created by lipy on 2017/4/10 0010.
 */
public interface PedometerRepository {

    /**
     * 获取计步器每日步数
     */
    void getPedometerStep(IGetPedometerResult result);
}
