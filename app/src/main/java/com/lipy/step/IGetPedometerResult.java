package com.lipy.step;

import com.lipy.step.dao.core.PedometerEntity;

/**
 * Created by lipy on 2017/4/10 0010.
 */
public interface IGetPedometerResult {

    void onSuccessGet(final PedometerEntity cardEntity);
}
