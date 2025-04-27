package com.example.androidappscheduler.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidappscheduler.data.AlarmRepo
import com.example.androidappscheduler.data.InstalledAppRepository
import com.example.androidappscheduler.entries.AlarmLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "AlarmDetailViewModel"

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(private val repository: InstalledAppRepository, private val alarmRepo: AlarmRepo) :
    ViewModel() {

    private val _alarmListDataForDetail = MutableLiveData<List<AlarmLauncher>>()
    val alarmListDataForDetail: LiveData<List<AlarmLauncher>>
        get() = _alarmListDataForDetail


    private val _alarmDeleted = MutableLiveData<Boolean>()
    val alarmDeleted: LiveData<Boolean>
        get() = _alarmDeleted


    fun getAppName(packageName: String): String {
        return repository.getAppName(packageName)
    }

    fun getAlarmList(packageName: String) {
        val alarmList = ArrayList<AlarmLauncher>()
        CoroutineScope(Dispatchers.IO).launch {
            repository.getAlarmByPackageName(packageName).collect { it ->
                alarmList.add(it)
            }
            _alarmListDataForDetail.postValue(alarmList)
        }
    }

    fun deleteAlarm(alarmId: Int, context: Context, packageName: String) {
        Log.d(TAG, "deleteAlarm: onAlarm Delete ${alarmId}")
        CoroutineScope(Dispatchers.IO).launch {
            alarmRepo.cancelAnAlarm(context, alarmId, packageName)
            val result = repository.deleteAlarm(alarmId)
            Log.d(TAG, "deleteAlarm: onAlarm Delete $alarmId done result $result")
            _alarmDeleted.postValue(result > 0)
        }
    }
}