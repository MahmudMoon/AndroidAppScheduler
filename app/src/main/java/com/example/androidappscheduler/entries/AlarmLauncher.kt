package com.example.androidappscheduler.entries

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmLauncher(
    @PrimaryKey(autoGenerate = true) val alarmId: Long = 0,
    val packageName: String,
    val launchTime: Long,
    val isLaunched: Boolean
)