package com.example.androidappscheduler.data

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class InstalledAppRepository @Inject constructor(
    private val packageManager: PackageManager
) {
    private val _installedAppListState = MutableStateFlow<List<String>>(emptyList())
    val installedAppListState: StateFlow<List<String>>
        get() = _installedAppListState

    suspend fun getInstalledApps() {
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        packages.filter {
            packageManager.getLaunchIntentForPackage(it.packageName) != null
        }.map {
            it.packageName
        }.let { packageNames ->
            _installedAppListState.emit(packageNames)
        }
    }
}