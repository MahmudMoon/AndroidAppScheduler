package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.db.AlarmLauncherDb
import com.example.androidappscheduler.entries.AlarmLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AlarmReceiver"

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmLauncherDao: AlarmLauncherDao

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
            Log.d(TAG, "launchApp:AlarmLauncherDb: "+ alarmLauncherDao)
            if(alarmLauncherDao!=null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val launchableAlarms = alarmLauncherDao.getAllAlarms()
                    Log.d(TAG, "launchApp: " + launchableAlarms.size)
                    if (launchableAlarms.size == 0) {
                        alarmLauncherDao.insertAlarm(
                            AlarmLauncher(
                                packageName = packageName,
                                launchTime = System.currentTimeMillis(),
                                isLaunched = false
                            )
                        )
                    } else {
                        launchableAlarms.forEach {
                            Log.d(TAG, "launchApp:AlarmLauncher " + it.packageName)
                            Log.d(TAG, "launchApp:AlarmLauncher " + it.launchTime)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        context.startActivity(launchIntent)
                    }
                }
            }
        } else {
            Toast.makeText(context, "Application not found", Toast.LENGTH_SHORT).show()
        }
    }
}