package com.example.androidappscheduler.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.db.AlarmLauncherDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }

    @Singleton
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Singleton
    @Provides
    fun provideAlarmDatabase(@ApplicationContext context: Context): AlarmLauncherDb {
        return Room.databaseBuilder(
            context,
            AlarmLauncherDb::class.java,
            AlarmLauncherDb.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideAlarmLauncherDao(db: AlarmLauncherDb): AlarmLauncherDao {
        return db.alarmDao()
    }
}