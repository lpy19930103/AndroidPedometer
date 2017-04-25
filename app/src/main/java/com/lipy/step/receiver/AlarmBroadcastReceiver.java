package com.lipy.step.receiver;

import com.lipy.step.pedometer.PedometerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lipy on 2017/4/19 0019.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("lipy","AlarmBroadcastReceiver onReceive");
        Intent mService = new Intent(context, PedometerService.class);
        context.startService(mService);
    }
}
