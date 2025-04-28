package com.example.androidappscheduler.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.androidappscheduler.receiver.AlarmReceiver
import javax.inject.Inject


class AlarmRepo @Inject constructor(
    private val alarmManager: AlarmManager,
) {
    private val TAG = "AlarmRepo"

    fun cancelAnAlarm(context: Context, alarmID: Int, packageName: String) {
        Log.d(TAG, "cancelAnAlarm: $alarmID")
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

        } catch (e: Exception) {
            Log.e(TAG, "cancelAnAlarm: ${e.printStackTrace()}")
        }

    }

    fun setAnAlarm(context: Context, alarmID: Int, alarmTime: Long, packageName: String) {
        Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("packageName", packageName)
            intent.putExtra("alarmID", alarmID)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                    //  Toast.makeText(context, "Alarm set for $packageName", Toast.LENGTH_SHORT).show()
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                // Toast.makeText(context, "Alarm set for $packageName", Toast.LENGTH_SHORT).show()
            }
        }
    }
}