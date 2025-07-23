package com.nutomic.syncthingandroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nutomic.syncthingandroid.service.SyncthingService;

/**
 * Broadcast-receiver to control and configure Syncthing remotely.
 */
public class AppConfigReceiver extends BroadcastReceiver {

    /**
     * Start the Syncthing-Service
     */
    private static final String ACTION_START = "com.nutomic.syncthingandroid.action.START";

    /**
     * Stop the Syncthing-Service
     * If startServiceOnBoot is enabled the service must not be stopped. Instead a
     * notification is presented to the user.
     */
    private static final String ACTION_STOP  = "com.nutomic.syncthingandroid.action.STOP";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_START:
                startServiceCompat(context);
                break;
            case ACTION_STOP:
                context.stopService(new Intent(context, SyncthingService.class));
                break;
        }
    }

    static void startServiceCompat(Context context) {
        Intent intent = new Intent(context, SyncthingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }
    }
}
