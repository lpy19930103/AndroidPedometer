package com.lipy.step;

import com.lipy.step.common.BaseApplication;
import com.lipy.step.common.PedometerEvent;
import com.lipy.step.dao.PedometerEntity;
import com.lipy.step.dao.PedometerEntityDao;
import com.lipy.step.pedometer.ApplicationModule;
import com.lipy.step.pedometer.PedometerRepository;
import com.lipy.step.result.IGetPedometerResult;
import com.lipy.step.result.PedometerUpDateResult;
import com.lipy.step.utils.HardwarePedometerUtil;
import com.lipy.step.utils.PermissionUtils;

import org.greenrobot.eventbus.Subscribe;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView mTvSteps;

    TextView mTvAccelerometer;
    TextView mTvStepCounter;
    TextView mTvTargetSteps;
    TextView mSdkVer;

    private PedometerRepository mPedometerRepository;

    private List<String> mPermissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvAccelerometer = (TextView) findViewById(R.id.tv_accelerometer);
        mTvStepCounter = (TextView) findViewById(R.id.tv_step_counter);
        mTvTargetSteps = (TextView) findViewById(R.id.tv_target_steps);
        mSdkVer = (TextView) findViewById(R.id.tv_sdk_v);
        mTvSteps = (TextView) findViewById(R.id.tv_steps);
        mPermissions.add(Manifest.permission.BODY_SENSORS);
        mPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionUtils.requestMultiPermissions(this, mPermissions, new PermissionUtils.PermissionGrant() {
                    @Override
                    public void onPermissionGranted(String requestPermission) {

                    }
                }
        );


        init();
        ApplicationModule.getInstance().getPedometerManager().checkServiceStart();
        ApplicationModule.getInstance().getPedometerManager().setPedometerUpDateResult(new PedometerUpDateResult() {
            @Override
            public void onPedometerUpDate(PedometerEntity entity) {
                mTvSteps.setText(entity.getDailyStep() + "步");
                mTvTargetSteps.setText("日期：" + entity.getDate());
            }
        });
    }

    private void init() {
        BaseApplication.globalRegisterEvent(this);
        //第一次初始化数据:包括退出应用后再进入的数据初始化
        getPedometerStep();

        if (HardwarePedometerUtil.supportsHardwareAccelerometer(this)) {
            mTvAccelerometer.setText("是");
        } else {
            mTvAccelerometer.setText("否");
        }

        if (HardwarePedometerUtil.supportsHardwareStepCounter(this)) {
            mTvStepCounter.setText("是");
        } else {
            mTvStepCounter.setText("否");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSdkVer.setText("" + Build.VERSION.SDK_INT);
        BaseApplication.getInstances().setDatabase(this);
        PedometerEntityDao pedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
        List<PedometerEntity> pedometerEntities = pedometerEntityDao.loadAll();
        if (pedometerEntityDao != null && pedometerEntities != null && pedometerEntities.size() > 0) {
            PedometerEntity pedometerEntity = pedometerEntities.get(pedometerEntities.size() - 1);
            mTvSteps.setText(pedometerEntity.getDailyStep() + "步");
            mTvTargetSteps.setText("日期：" + pedometerEntity.getDate());
        }
    }

    /**
     * 获取计步器的数据
     */
    public void getPedometerStep() {
        mPedometerRepository = ApplicationModule.getInstance().getPedometerRepository();
        mPedometerRepository.getPedometerStep(new IGetPedometerResult() {
            @Override
            public void onSuccessGet(PedometerEntity cardEntity) {
                if (cardEntity != null) {
                    mTvSteps.setText(cardEntity.getDailyStep() + "步");
//                    mTvTargetSteps.setText("目标步数：" + cardEntity.getTargetStepCount() + "步");
                }
            }
        });
    }

    /**
     * 监听数据
     */
    @Subscribe
    public void onEventMainThread(PedometerEvent event) {
        Log.e("lipy", "onEventMainThread");
        if (event.mIsUpdate) {
            getPedometerStep();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ApplicationModule.getInstance().getPedometerManager().unbindPedometerService();
        super.onDestroy();
    }
}
