package com.example.androidappscheduler.viewmodels

import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

private const val TAG = "MainActivityViewModel"

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val installedAppRepository: InstalledAppRepository,
    private val alarmRepo: AlarmRepo
) : ViewModel() {

    private val _installedAppListState = MutableLiveData<List<PackageInstance>>(emptyList())
    val installedAppList: LiveData<List<PackageInstance>>
        get() = _installedAppListState

    private val _successfullyStoredAlarm = MutableLiveData<Boolean>(false)
    val successfullyStoredAlarm: LiveData<Boolean>
        get() = _successfullyStoredAlarm

    init {
        viewModelScope.launch {
            getInstalledApps()
        }
    }

    fun saveAlarm(context: Context, uniqueRequestCode: Int, packageName: String, alarmTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepo.setAnAlarm(context = context, uniqueRequestCode, alarmTime, packageName)
            installedAppRepository.saveAnewAlarm(uniqueRequestCode, packageName, alarmTime).apply {
                if (this > 0) {
                    Log.d(TAG, "saveAlarm: Alarm saved successfully")
                    _successfullyStoredAlarm.postValue(true)
                } else {
                    Log.d(TAG, "saveAlarm: Failed to save alarm")
                    _successfullyStoredAlarm.postValue(false)
                }
            }
        }
    }

    fun getInstalledApps() {
        CoroutineScope(Dispatchers.IO).launch {
            val installedApps = ArrayList<PackageInstance>()
            installedAppRepository.getInstalledApps().collect() { data ->
                installedApps.add(data)
            }
            _installedAppListState.postValue(installedApps)
        }
    }

    fun getSavedAlarmList() {
        CoroutineScope(Dispatchers.IO).launch {
            installedAppRepository.getSavedAlarmList().collect() { data ->
                Log.d("TEST_DATA", "getSavedAlarmList: $data")
            }
        }
    }
}