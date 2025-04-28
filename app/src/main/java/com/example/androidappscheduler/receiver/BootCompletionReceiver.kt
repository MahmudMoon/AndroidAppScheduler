package com.example.androidappscheduler.receiver

import android.content.BroadcastReceiver
import android.util.Log

private const val TAG = "BootCompletionReceiver"

class BootCompletionReceiver: BroadcastReceiver() {
    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
        Log.d(TAG, "onReceive: intent: ${intent.action}")
        if (intent.action == android.content.Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "onReceive: Boot completed")
        }
    }
}