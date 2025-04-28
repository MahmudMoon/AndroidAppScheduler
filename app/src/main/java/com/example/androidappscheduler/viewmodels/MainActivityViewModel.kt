package com.example.androidappscheduler.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidappscheduler.data.AlarmRepo
import com.example.androidappscheduler.data.InstalledAppRepository
import com.example.androidappscheduler.entries.AlarmLauncher
import com.example.androidappscheduler.models.PackageInstance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val installedAppRepository: InstalledAppRepository, private val alarmRepo: AlarmRepo): ViewModel() {

    val installedAppListState: StateFlow<List<PackageInstance>>
        get() = installedAppRepository.installedAppListState

    val successfullyStoredAlarm: StateFlow<Boolean>
        get() = installedAppRepository.successfullyStoredAlarm

    init {
        viewModelScope.launch {
            getInstalledApps()
        }
    }

    fun saveAlarm(context: Context, uniqueRequestCode: Int, packageName: String, alarmTime: Long) {
        viewModelScope.launch {
            alarmRepo.setAnAlarm(context = context, uniqueRequestCode, alarmTime, packageName)
            installedAppRepository.saveAnewAlarm(uniqueRequestCode ,packageName, alarmTime)
        }
    }

    fun getInstalledApps(){
        installedAppRepository.getInstalledApps()
    }

    fun getSavedAlarmList(){
        CoroutineScope(Dispatchers.IO).launch {
          installedAppRepository.getSavedAlarmList().collect() { data ->
              Log.d("TEST_DATA", "getSavedAlarmList: $data")
          }
        }
    }
}