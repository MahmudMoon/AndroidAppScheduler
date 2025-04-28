package com.example.androidappscheduler.utils

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.Calendar

object Constants {
    const val SHARED_PREF_NAME = "app_scheduler_prefs"
    const val LAST_LAUNCH_TIME_KEY = "last_launch_time"
    const val ALARM_ACTION = "com.example.androidappscheduler.ALARM_ACTION"
    const val PACKAGE_NAME_KEY = "package_name_key"
    const val NOTIFICATION_CHANNEL_ID = "foreground_service_channel_for_launcher"
    const val NOTIFICATION_CHANNEL_NAME = "Foreground Service Channel"
    const val NOTIFICATION_ID = 1
    const val LAST_NOTIFICATION_ID = "last_notification_id"
    const val APP_NAME = "AndroidAppScheduler"

    fun saveInSP(context: Context, randomDelay: Long) {
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().let {
            it.putLong(LAST_LAUNCH_TIME_KEY, randomDelay)
            it.apply()
        }
    }

    fun getFromSP(context: Context): Long {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getLong(LAST_LAUNCH_TIME_KEY, 0L)
    }

    fun saveLastNotificationID(context: Context, notificationID: Int) {
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().let {
            it.putInt(LAST_NOTIFICATION_ID, notificationID)
            it.apply()
        }
    }

    fun getLastNotificationID(context: Context): Int {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getInt(LAST_NOTIFICATION_ID, 0)
    }

    fun openAlarmDialog(packageName: String, suggestedTime: Long = 0L ,context: Context, setAlarmForPackage: (String, Long) -> Unit) {
        val calendar = Calendar.getInstance()
        if(suggestedTime>0){
            calendar.timeInMillis = suggestedTime
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)

            AlertDialog.Builder(context).apply {
                setTitle("Set Alarm for $packageName")
                setMessage("Do you want to set an alarm for $packageName at ${selectedHour}:${String.format("%02d", selectedMinute)}?")
                setPositiveButton("Yes") { _, _ ->
                    if(calendar.timeInMillis > System.currentTimeMillis())
                        setAlarmForPackage(packageName, calendar.timeInMillis)
                    else{
                        Toast.makeText(context, "Can not set alarm to previos time", Toast.LENGTH_SHORT).show()
                    }
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }, hour, minute, true).show()
    }
}