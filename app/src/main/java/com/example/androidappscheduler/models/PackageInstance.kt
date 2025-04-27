package com.example.androidappscheduler.models

import com.example.androidappscheduler.entries.AlarmLauncher

data class PackageInstance(val packageName: String, val appName: String, val alarms: List<AlarmLauncher>)