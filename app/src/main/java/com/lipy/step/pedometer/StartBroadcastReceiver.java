package com.lipy.step.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机启动
 * Created by lipy on 2017/4/10 0010.
 */
public class StartBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)){
            Intent mService = new Intent(context, PedometerService.class);
            context.startService(mService);
            ActionModule.getInstance().getPedometerManager().IsServiceRunning = true;
        }
    }
}
