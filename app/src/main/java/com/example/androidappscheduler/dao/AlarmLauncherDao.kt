package com.example.androidappscheduler.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.androidappscheduler.entries.AlarmLauncher

@Dao
interface AlarmLauncherDao {
    @Insert
    fun insertAlarm(alarmEntity: AlarmLauncher)

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): List<AlarmLauncher>

    @Delete
    fun deleteAlarm(alarmEntity: AlarmLauncher)
}