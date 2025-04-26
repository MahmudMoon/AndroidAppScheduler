package com.example.androidappscheduler.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.entries.AlarmLauncher

@Database(entities = [AlarmLauncher::class], version = 1, exportSchema = false)
abstract class AlarmLauncherDb: RoomDatabase() {
    abstract fun alarmDao(): AlarmLauncherDao

    companion object {
        const val DATABASE_NAME = "alarm_launcher_db"
    }
}