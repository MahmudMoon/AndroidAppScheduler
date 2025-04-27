package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import com.example.androidappscheduler.services.LauncherForegroundService

private const val TAG = "AlarmReceiver"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Alarm Triggered")
        if (context != null && intent != null) {
            val packageName = intent.getStringExtra("packageName") ?: ""
            val alarmID = intent.getIntExtra("alarmID", 0)
            Log.d(TAG, "onReceive: Package Name: $packageName")


            val serviceIntent = Intent(context, LauncherForegroundService::class.java)
            serviceIntent.putExtra("packageName", packageName)
            serviceIntent.putExtra("alarmID", alarmID)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent);
            }

        } else {
            Log.d(TAG, "onReceive: Context or Intent is null")
        }
    }

}