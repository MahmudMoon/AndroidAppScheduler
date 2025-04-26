package com.example.androidappscheduler.ui

import android.Manifest
import android.app.AlarmManager
import android.app.ComponentCaller
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.adapters.InstalledPackageAdapter
import com.example.androidappscheduler.receiver.AlarmReceiver
import com.example.androidappscheduler.viewmodels.InstalledAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Random
import javax.inject.Inject

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var installedPackageAdapter: InstalledPackageAdapter
    private lateinit var recyclerView: RecyclerView

    @Inject
    lateinit var installedAppViewModel: InstalledAppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAlarmPermission()
        }

        installedPackageAdapter = InstalledPackageAdapter(this, emptyList()) { packageName ->
            setAlarmForPackage(packageName)
        }

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        installedAppViewModel.installedAppListState.value.let {
            Log.d(TAG, "onCreate: $it")
            installedPackageAdapter = InstalledPackageAdapter(this, it) { packageName ->
                //launchApp(packageName)
                setAlarmForPackage(packageName)
            }
            recyclerView.adapter = installedPackageAdapter
            installedPackageAdapter.notifyDataSetChanged()
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAlarmPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), 104)
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

    private fun setAlarmForPackage(packageName: String) {
        // Set alarm for the package
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        Intent(this, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("packageName", packageName)
            val alarmTime = System.currentTimeMillis() + 60000 * 2 // 2 minute from now

            val uniqueRequestCode = alarmTime.toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                uniqueRequestCode ,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                    Toast.makeText(this, "Alarm set for $packageName", Toast.LENGTH_SHORT).show()
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                Toast.makeText(this, "Alarm set for $packageName", Toast.LENGTH_SHORT).show()
            }

            Log.d(TAG, "setAlarmForPackage: $packageName RequestCode: $uniqueRequestCode")

        }

    }
}