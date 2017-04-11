package com.lipy.step;

import com.lipy.step.dao.core.PedometerEntity;
import com.lipy.step.dao.core.PedometerEntityDao;

import org.greenrobot.eventbus.Subscribe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    TextView tvSteps;

    TextView tvAccelerometer;
    TextView tvStepCounter;
    TextView tvTargetSteps;
    private boolean mIsStart = false;

    private PedometerRepository mPedometerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAccelerometer = (TextView) findViewById(R.id.tv_accelerometer);
        tvStepCounter = (TextView) findViewById(R.id.tv_step_counter);
        tvTargetSteps = (TextView) findViewById(R.id.tv_target_steps);
        tvSteps = (TextView) findViewById(R.id.tv_steps);
//        btnStart = (Button) findViewById(R.id.btn_start);
//        btnStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mIsStart) {
//                    mIsStart = true;
//                    ApplicationModule.getInstance().getPedometerManager().startPedometerService();
//                    btnStart.setText("停止计步");
//                } else {
//                    mIsStart = false;
//                    ApplicationModule.getInstance().getPedometerManager().stopPedometerService();
//                    btnStart.setText("开始计步");
//                }
//
//            }
//        });
        init();
        ApplicationModule.getInstance().getPedometerManager().checkServiceStart();
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
        BaseApplication.getInstances().setDatabase(this);
        PedometerEntityDao pedometerEntityDao = BaseApplication.getInstances().getDaoSession().getPedometerEntityDao();
        if (pedometerEntityDao != null) {
            PedometerEntity pedometerEntity = pedometerEntityDao.loadAll().get(pedometerEntityDao.loadAll().size() - 1);
            tvSteps.setText(pedometerEntity.getTargetStepCount() + "步");
            tvTargetSteps.setText("日期：" + pedometerEntity.getDate() );
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
                    tvSteps.setText(cardEntity.getStepCount() + "步");
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


}
