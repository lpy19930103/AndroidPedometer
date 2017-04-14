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

    TextView tvSteps;

    TextView tvAccelerometer;
    TextView tvStepCounter;
    TextView tvTargetSteps;
    TextView sdkVer;

    private PedometerRepository mPedometerRepository;

    private List<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAccelerometer = (TextView) findViewById(R.id.tv_accelerometer);
        tvStepCounter = (TextView) findViewById(R.id.tv_step_counter);
        tvTargetSteps = (TextView) findViewById(R.id.tv_target_steps);
        sdkVer = (TextView) findViewById(R.id.tv_sdk_v);
        tvSteps = (TextView) findViewById(R.id.tv_steps);
        permissions.add(Manifest.permission.BODY_SENSORS);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PermissionUtils.requestMultiPermissions(this, permissions, new PermissionUtils.PermissionGrant() {
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
                tvSteps.setText(entity.getDailyStep() + "步");
                tvTargetSteps.setText("日期：" + entity.getDate());
            }
        });
    }

    private void init() {
        BaseApplication.globalRegisterEvent(this);
        //第一次初始化数据:包括退出应用后再进入的数据初始化
        getPedometerStep();

        if (HardwarePedometerUtil.supportsHardwareAccelerometer(this)) {
            tvAccelerometer.setText("是");
        } else {
            tvAccelerometer.setText("否");
        }

        if (HardwarePedometerUtil.supportsHardwareStepCounter(this)) {
            tvStepCounter.setText("是");
        } else {
            tvStepCounter.setText("否");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sdkVer.setText("" + Build.VERSION.SDK_INT);
        BaseApplication.getInstances().setDatabase(this);
        PedometerEntityDao pedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
        List<PedometerEntity> pedometerEntities = pedometerEntityDao.loadAll();
        if (pedometerEntityDao != null && pedometerEntities != null && pedometerEntities.size() > 0) {
            PedometerEntity pedometerEntity = pedometerEntities.get(pedometerEntities.size() - 1);
            tvSteps.setText(pedometerEntity.getDailyStep() + "步");
            tvTargetSteps.setText("日期：" + pedometerEntity.getDate());
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
                    tvSteps.setText(cardEntity.getDailyStep() + "步");
//                    tvTargetSteps.setText("目标步数：" + cardEntity.getTargetStepCount() + "步");
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
