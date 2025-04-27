package com.example.androidappscheduler.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.androidappscheduler.receiver.AlarmReceiver
import com.example.androidappscheduler.services.LauncherForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AlarmRepo @Inject constructor(
    private val alarmManager: AlarmManager,
) {
    private val TAG = "AlarmRepo"

    fun cancelAnAlarm( context: Context, alarmID: Int, packageName: String) {
        Log.d(TAG, "cancelAnAlarm: $alarmID")
//        val intent = Intent(context, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, alarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//        alarmManager.cancel(pendingIntent)

        try {
            Intent(context, AlarmReceiver::class.java).let { intent ->
                val uniqueRequestCode = alarmID
                intent.putExtra("packageName", packageName)
                intent.putExtra("alarmID", uniqueRequestCode)

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueRequestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel {
                    pendingIntent
                }
                pendingIntent.cancel()
            }

        }catch (e: Exception) {
            Log.e(TAG, "cancelAnAlarm: ${e.printStackTrace()}")
        }

    }
}