package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
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

                var randomDelay = System.currentTimeMillis() + 10000;
                val handler = Handler(Looper.getMainLooper())

                val lastLaunchTime = getFromSP(context);

                randomDelay = maxOf(randomDelay, lastLaunchTime + 10000)

                saveInSP(context, randomDelay);


                Log.d(TAG, "onReceive: Current Time" + System.currentTimeMillis())
                Log.d(TAG, "onReceive: Scheduled Time: " + randomDelay);
                Log.d(TAG, "onReceive: Last launched Scheduled " + lastLaunchTime)
                Log.d(TAG, "onReceive: =============================================")
                Log.d(TAG, "onReceive: Diffence from now: ${(randomDelay - System.currentTimeMillis())/1000}")
                handler.postDelayed({
                    launchApp(context, packageName)
                }, randomDelay - System.currentTimeMillis())

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

    fun saveInSP(context: Context, randomDelay: Long) {
        context.getSharedPreferences("alarm_delay_sp", Context.MODE_PRIVATE).edit().let {
            it.putLong("lastLaunchTime", randomDelay)
            it.apply()
        }
    }

    fun getFromSP(context: Context): Long {
        return context.getSharedPreferences("alarm_delay_sp", Context.MODE_PRIVATE)
            .getLong("lastLaunchTime", 0L)
    }
}