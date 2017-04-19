package com.lipy.step;

import com.lipy.step.common.Constants;
import com.lipy.step.pedometer.ApplicationModule;
import com.lipy.step.utils.HardwarePedometerUtil;
import com.lipy.step.utils.PermissionUtils;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lipy on 2017/4/10 0009.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    TextView mTvAccelerometer;
    TextView mTvStepCounter;
    TextView mSdkVer;


    private List<String> mPermissions = new ArrayList<>();
    private EditText mStepEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvAccelerometer = (TextView) findViewById(R.id.tv_accelerometer);
        mTvStepCounter = (TextView) findViewById(R.id.tv_step_counter);
        mSdkVer = (TextView) findViewById(R.id.tv_sdk_v);
        findViewById(R.id.set_step).setOnClickListener(this);
        findViewById(R.id.check_step).setOnClickListener(this);
        mStepEt = (EditText) findViewById(R.id.set_step_et);

        //权限请求
        mPermissions.add(Manifest.permission.BODY_SENSORS);
        mPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        PermissionUtils.requestMultiPermissions(this, mPermissions, new PermissionUtils.PermissionGrant() {
                    @Override
                    public void onPermissionGranted(String requestPermission) {

                    }
                }
        );

        init();

    }

    private void init() {

        ApplicationModule.getInstance().getPedometerManager().checkServiceStart();

        mSdkVer.setText("" + Build.VERSION.SDK_INT);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_step:
                startActivity(new Intent(this, PedometerActivity.class));
                break;
            case R.id.set_step:
                Constants.TAG_STEP = Integer.parseInt(mStepEt.getText().toString().trim());
                break;
        }

    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ApplicationModule.getInstance().getPedometerManager().unbindPedometerService();
            finish();
            return true;
        }
        return false;
    }




}
