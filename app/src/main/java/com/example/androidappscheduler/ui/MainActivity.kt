package com.example.androidappscheduler.ui

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.adapters.InstalledPackageAdapter
import com.example.androidappscheduler.utils.Constants.openAlarmDialog
import com.example.androidappscheduler.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var installedPackageAdapter: InstalledPackageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Installed Apps"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAlarmPermission()
        }

        installedPackageAdapter = InstalledPackageAdapter(this, emptyList()) { packageName ->
            //launchApp(packageName)
            //setAlarmForPackage(packageName)
            openAlarmDialog(packageName = packageName, context = this) { packName, alarmTime ->
                setAlarmForPackage(packName, alarmTime)
            }
        }

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        mainActivityViewModel.installedAppList.observe(this) {
            Log.d(TAG, "onCreate: $it")
            installedPackageAdapter = InstalledPackageAdapter(this, it, { packageName ->
                onAlarmDetailClicked(packageName)
            }) { packageName ->
                openAlarmDialog(packageName = packageName, context = this) { packName, alarmTime ->
                    setAlarmForPackage(packName, alarmTime)
                }
            }
            recyclerView.adapter = installedPackageAdapter
            installedPackageAdapter.notifyDataSetChanged()
        }

        mainActivityViewModel.successfullyStoredAlarm.observe(this) {
            Log.d(TAG, "onCreate: Successfully stored alarm: $it")
            if (it) {
                mainActivityViewModel.getInstalledApps()
                Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to set alarm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onAlarmDetailClicked(packageName: String) {
        // Handle alarm detail click
        Log.d(TAG, "onAlarmDetailClicked: $packageName")
        val intent = Intent(this, AlarmDetailActivity::class.java)
        intent.putExtra("packageName", packageName)
        startActivity(intent)
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAlarmPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SCHEDULE_EXACT_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                104
            )
        } else {
            Log.d(TAG, "checkAlarmPermission: Permission granted")
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if (requestCode == 104) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: Permission granted")

            } else {
                Log.d(TAG, "onActivityResult: Permission denied")
            }
        }
    }

    private fun setAlarmForPackage(packageName: String, alarmTime: Long) {
        val uniqueRequestCode = System.currentTimeMillis().hashCode()
        mainActivityViewModel.saveAlarm(this@MainActivity, uniqueRequestCode, packageName, alarmTime)
        Log.d(TAG, "setAlarmForPackage: $packageName RequestCode: $uniqueRequestCode")
        mainActivityViewModel.getInstalledApps()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        mainActivityViewModel.getInstalledApps()

        //test purpose
       // mainActivityViewModel.getSavedAlarmList()
    }


}
