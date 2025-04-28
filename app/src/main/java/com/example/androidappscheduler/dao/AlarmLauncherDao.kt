package com.example.androidappscheduler.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.androidappscheduler.entries.AlarmLauncher

@Dao
interface AlarmLauncherDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAlarm(alarmEntity: AlarmLauncher): Long

    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): List<AlarmLauncher>

    @Query("DELETE FROM alarms WHERE alarmId = :alarmId")
    fun deleteAlarmById(alarmId: Int): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun markAlarmAsLaunched(alarmEntity: AlarmLauncher): Int

    @Query("SELECT * FROM alarms WHERE alarmId = :alarmId")
    fun getAlarmById(alarmId: Int): AlarmLauncher

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlarm(alarmLauncher: AlarmLauncher): Int
}
