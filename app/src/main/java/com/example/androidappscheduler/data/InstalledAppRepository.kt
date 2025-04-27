package com.example.androidappscheduler.data

import android.content.pm.PackageManager
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.entries.AlarmLauncher
import com.example.androidappscheduler.models.PackageInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class InstalledAppRepository @Inject constructor(
    private val packageManager: PackageManager,
    private val alarmLauncherDao: AlarmLauncherDao
) {
    private val _installedAppListState = MutableStateFlow<List<PackageInstance>>(emptyList())
    val installedAppListState: StateFlow<List<PackageInstance>>
        get() = _installedAppListState

    private val _successfullyStoredAlarm = MutableStateFlow<Boolean>(false)
    val successfullyStoredAlarm: StateFlow<Boolean>
        get() = _successfullyStoredAlarm

    private val _successfullyUpdatedAlarm = MutableStateFlow<Boolean>(false)
    val successfullyUpdatedAlarm: StateFlow<Boolean>
        get() = _successfullyUpdatedAlarm


    private val _alarmListData = MutableStateFlow<List<AlarmLauncher>>(emptyList())
    val alarmListData: StateFlow<List<AlarmLauncher>>
        get() = _alarmListData

    fun getInstalledApps() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedAlarms = alarmLauncherDao.getAllAlarms()

            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.filter {
                packageManager.getLaunchIntentForPackage(it.packageName) != null
            }.map { packageInfo ->
                val filteredAlarms =
                    savedAlarms.filter { it.packageName == packageInfo.packageName && it.isLaunched == false }
                PackageInstance(
                    packageName = packageInfo.packageName,
                    appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString(),
                    alarms = filteredAlarms
                )
            }.let { packageInstances ->
                _installedAppListState.emit(packageInstances)
            }
        }


    }

    fun saveAnewAlarm(alarmId: Int, packageName: String, time: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLauncherDao.insertAlarm(
                AlarmLauncher(
                    alarmId = alarmId,
                    packageName = packageName,
                    launchTime = time,
                    isLaunched = false
                )
            ).apply {
                if (this > 0) {
                    _successfullyStoredAlarm.emit(true)
                } else {
                    _successfullyStoredAlarm.emit(false)
                }
            }
        }
    }

    fun getSavedAlarmList() {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLauncherDao.getAllAlarms().map {
                AlarmLauncher(
                    alarmId = it.alarmId,
                    packageName = it.packageName,
                    launchTime = it.launchTime,
                    isLaunched = it.isLaunched
                )
            }.let { alarms ->
                _alarmListData.emit(alarms)
            }
        }
    }

    fun deleteAlarm(alarmId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmLauncherDao.deleteAlarmById(alarmId)
        }
    }

    fun getAlarmById(alarmId: Int): AlarmLauncher? {
        return alarmLauncherDao.getAlarmById(alarmId)
    }

    fun markAlarmAsLaunched(alarmId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            getAlarmById(alarmId)?.let {
                val updatedAlarm = it.copy(isLaunched = true)
                alarmLauncherDao.markAlarmAsLaunched(updatedAlarm).apply {
                    if (this > 0) {
                        _successfullyUpdatedAlarm.emit(true)
                    } else {
                        _successfullyUpdatedAlarm.emit(false)
                    }
                }
            }
        }
    }
}