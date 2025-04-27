package com.example.androidappscheduler.entries

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmLauncher(
    @PrimaryKey val alarmId: Int,
    val packageName: String,
    val launchTime: Long,
    val isLaunched: Boolean
)