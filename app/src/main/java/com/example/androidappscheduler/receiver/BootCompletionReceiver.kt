package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.util.Log
import com.example.androidappscheduler.data.AlarmRepo
import com.example.androidappscheduler.data.InstalledAppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BootCompletionReceiver"

@AndroidEntryPoint
class BootCompletionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var installedAppRepository: InstalledAppRepository

    @Inject
    lateinit var alarmRepo: AlarmRepo
    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
        Log.d(TAG, "onReceive: intent: ${intent.action}")
        if (intent.action == android.content.Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "onReceive: Boot completed")
            CoroutineScope(Dispatchers.IO).launch {
                installedAppRepository.getSavedAlarmList().collect {
                    if (!it.isLaunched) {
                        Log.d(TAG, "===================================================")
                        Log.d(TAG, "onReceive:Package ${it.packageName}")
                        Log.d(TAG, "onReceive:AlarmId ${it.alarmId}")
                        Log.d(TAG, "onReceive:LaunchTime ${it.launchTime}")
                        Log.d(TAG, "onReceive:is Launched ${it.isLaunched}")
                        alarmRepo.setAnAlarm(context, it.alarmId, it.launchTime, it.packageName)
                    }
                }
            }

        }
    }
}