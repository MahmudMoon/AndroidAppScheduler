package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.example.androidappscheduler.services.LauncherForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AlarmReceiver"

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Alarm Triggered")
        if (context != null && intent != null) {
            val packageName = intent.getStringExtra("packageName") ?: ""
            Log.d(TAG, "onReceive: Package Name: $packageName")

            val serviceIntent = Intent(context, LauncherForegroundService::class.java)
            serviceIntent.putExtra("packageName", packageName)


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