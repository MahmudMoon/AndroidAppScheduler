package com.example.androidappscheduler.viewmodels

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidappscheduler.data.InstalledAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class InstalledAppViewModel @Inject constructor(private val installedAppRepository: InstalledAppRepository): ViewModel() {
    val installedAppListState: StateFlow<List<String>>
        get() = installedAppRepository.installedAppListState

    init {
        viewModelScope.launch {
            installedAppRepository.getInstalledApps()
        }
    }
}