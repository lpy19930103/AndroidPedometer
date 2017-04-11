package com.lipy.step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lipy on 2017/4/10 0010.
 */
public class StartBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("lipy","StartBroadcastReceiver onReceive onReceive onReceive:"+intent.getAction());
        if (intent.getAction().equals(ACTION)){
            Log.e("lipy","StartBroadcastReceiver onReceive onReceive onReceive");
            Intent mService = new Intent(context, PedometerService.class);
            context.startService(mService);
            ApplicationModule.getInstance().getPedometerManager().IsServiceRunning = true;
        }
    }
}
