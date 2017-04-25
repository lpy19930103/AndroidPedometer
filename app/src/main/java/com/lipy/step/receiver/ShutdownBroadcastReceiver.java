package com.lipy.step.receiver;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * 关机
 * Created by lipy on 2017/4/25 0025.
 */
public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ShutdownBroadcastReceiver";

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            Log.e("Shutdown", "ShutdownBroadcastReceiver onReceive()");
            BaseApplication.getInstances().setDatabase(context);
            PedometerEntityDao pedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
            List<PedometerEntity> pedometerEntities = pedometerEntityDao.loadAll();
            if (pedometerEntities != null && pedometerEntities.size() > 0) {
                PedometerEntity pedometerEntity = pedometerEntities.get(pedometerEntities.size() - 1);
                pedometerEntity.setReStart(true);
                pedometerEntityDao.update(pedometerEntity);
            }
        }
    }
}
