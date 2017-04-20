package com.lipy.step;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;
import com.lipy.step.pedometer.ActionModule;
import com.lipy.step.result.PedometerUpDateResult;
import com.lipy.step.view.AnnulusProgress;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

/**
 * Created by lipy on 2017/4/17 0002.
 */

public class PedometerActivity extends AppCompatActivity {

    private AnnulusProgress mAnnulusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);
        mAnnulusProgress = (AnnulusProgress) findViewById(R.id.pedometer_progress);
        ActionModule.getInstance().getPedometerManager().setPedometerUpDateResult(new PedometerUpDateResult() {
            @Override
            public void onPedometerUpDate(PedometerEntity entity) {
                mAnnulusProgress.setProgress(entity.getDailyStep());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        PedometerEntityDao pedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
        List<PedometerEntity> pedometerEntities = pedometerEntityDao.loadAll();
        if (pedometerEntityDao != null && pedometerEntities != null && pedometerEntities.size() > 0) {
            PedometerEntity pedometerEntity = pedometerEntities.get(pedometerEntities.size() - 1);
            Log.e("lipy", "pedometerEntity.getTagStep()" + pedometerEntity.getTagStep());
            Log.e("lipy", "pedometerEntity.getDailyStep()" + pedometerEntity.getDailyStep());
            mAnnulusProgress.setMaxValue(pedometerEntity.getTagStep());
            mAnnulusProgress.setProgress(pedometerEntity.getDailyStep());
        }
    }

}
