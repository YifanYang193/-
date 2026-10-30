package com.gentlealarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public final class ActionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        try {
            if (AlarmActions.SNOOZE.equals(intent.getAction())) AlarmActions.snooze(context);
            else if (AlarmActions.STOP.equals(intent.getAction())) AlarmActions.stop(context);
        } catch (RuntimeException error) {
            new AlarmState(context).note("操作失败：" + error.getMessage());
            Toast.makeText(context, "操作失败，请打开闹钟查看", Toast.LENGTH_LONG).show();
        }
    }
}
