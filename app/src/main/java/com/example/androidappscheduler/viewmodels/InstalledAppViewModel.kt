package com.example.androidappscheduler.viewmodels

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidappscheduler.data.InstalledAppRepository
import com.example.androidappscheduler.entries.AlarmLauncher
import com.example.androidappscheduler.models.PackageInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class InstalledAppViewModel @Inject constructor(private val installedAppRepository: InstalledAppRepository): ViewModel() {

    val installedAppListState: StateFlow<List<PackageInstance>>
        get() = installedAppRepository.installedAppListState

    val successfullyStoredAlarm: StateFlow<Boolean>
        get() = installedAppRepository.successfullyStoredAlarm

    val alarmListData: StateFlow<List<AlarmLauncher>>
        get() = installedAppRepository.alarmListData


    init {
        viewModelScope.launch {
            getInstalledApps()
        }
    }

    fun saveAlarm(uniqueRequestCode: Int, packageName: String, alarmTime: Long) {
        viewModelScope.launch {
            installedAppRepository.saveAnewAlarm(uniqueRequestCode ,packageName, alarmTime)
        }
    }

    fun getInstalledApps(){
        installedAppRepository.getInstalledApps()
    }

    fun getSavedAlarmList(){
        installedAppRepository.getSavedAlarmList()
    }
}