package com.example.androidappscheduler.data

import android.content.pm.PackageManager
import android.util.Log
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.entries.AlarmLauncher
import com.example.androidappscheduler.models.PackageInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

private const val TAG = "InstalledAppRepository"


class InstalledAppRepository @Inject constructor(
    private val packageManager: PackageManager,
    private val alarmLauncherDao: AlarmLauncherDao
) {

    fun getInstalledApps(): Flow<PackageInstance> {
        val savedAlarms = alarmLauncherDao.getAllAlarms()
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        return packages.filter {
            packageManager.getLaunchIntentForPackage(it.packageName) != null
        }.map { packageInfo ->
            val filteredAlarms =
                savedAlarms.filter { it.packageName == packageInfo.packageName }
            PackageInstance(
                packageName = packageInfo.packageName,
                appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString(),
                alarms = filteredAlarms
            )
        }.asFlow()
    }

    fun saveAnewAlarm(alarmId: Int, packageName: String, time: Long): Long {
        return alarmLauncherDao.insertAlarm(
            AlarmLauncher(
                alarmId = alarmId,
                packageName = packageName,
                launchTime = time,
                isLaunched = false
            )
        )
    }

    fun getSavedAlarmList(): Flow<AlarmLauncher> {
        return alarmLauncherDao.getAllAlarms().map {
            AlarmLauncher(
                alarmId = it.alarmId,
                packageName = it.packageName,
                launchTime = it.launchTime,
                isLaunched = it.isLaunched
            )
        }.asFlow()
    }

    fun deleteAlarm(alarmId: Int): Int {
        return alarmLauncherDao.deleteAlarmById(alarmId)
    }

    private fun getAlarmById(alarmId: Int): AlarmLauncher? {
        return alarmLauncherDao.getAlarmById(alarmId)
    }

    fun getAlarmByPackageName(packageName: String): Flow<AlarmLauncher> {
        return alarmLauncherDao.getAllAlarms().filter {
            it.packageName == packageName
        }.asFlow()
    }

    fun markAlarmAsLaunched(alarmId: Int) {
        getAlarmById(alarmId)?.let {
            val updatedAlarm = it.copy(isLaunched = true)
            alarmLauncherDao.markAlarmAsLaunched(updatedAlarm)
        }
    }

    fun getAppName(packageName: String): String {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            return packageManager.getApplicationLabel(applicationInfo).toString()

        } catch (nameException: PackageManager.NameNotFoundException) {
            Log.e(TAG, "onStartCommand: " + nameException.printStackTrace())
        }
        return ""
    }

    fun updateAlarm(alarmLauncher: AlarmLauncher): Int {
        return alarmLauncherDao.updateAlarm(alarmLauncher)
    }
}