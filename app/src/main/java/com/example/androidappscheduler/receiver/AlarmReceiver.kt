package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
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
            if (packageName.isNotEmpty()) {
                launchApp(context, packageName)
            } else
                Log.d(TAG, "onReceive: Package Name is empty")
        } else {
            Log.d(TAG, "onReceive: Context or Intent is null")
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        val packageManager: PackageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        Log.d(TAG, "launchApp: $packageName")
        Log.d(TAG, "launchApp: $launchIntent")
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "Application not found", Toast.LENGTH_SHORT).show()
        }
    }
}