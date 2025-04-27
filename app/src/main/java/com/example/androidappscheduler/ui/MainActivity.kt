package com.example.androidappscheduler.ui

import android.Manifest
import android.app.AlarmManager
import android.app.ComponentCaller
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.adapters.InstalledPackageAdapter
import com.example.androidappscheduler.receiver.AlarmReceiver
import com.example.androidappscheduler.viewmodels.InstalledAppViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
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
            openAlarmDialog(packageName)
        }

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        installedAppViewModel.installedAppListState.asLiveData().observe(this){
            Log.d(TAG, "onCreate: $it")
            installedPackageAdapter = InstalledPackageAdapter(this, it) { packageName ->
                //launchApp(packageName)
                //setAlarmForPackage(packageName)
                openAlarmDialog(packageName)
            }
            recyclerView.adapter = installedPackageAdapter
            installedPackageAdapter.notifyDataSetChanged()
        }

        installedAppViewModel.successfullyStoredAlarm.asLiveData().observe(this){
            Log.d(TAG, "onCreate: Successfully stored alarm: $it")
            if (it) {
                Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to set alarm", Toast.LENGTH_SHORT).show()
            }
        }

        installedAppViewModel.alarmListData.asLiveData().observe(this) {
            Log.d(TAG, "onCreate: Alarm List Data: ${it.size}")
        }
    }

    private fun openAlarmDialog(packageName: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)

            AlertDialog.Builder(this).apply {
                setTitle("Set Alarm for $packageName")
                setMessage("Do you want to set an alarm for $packageName at ${selectedHour}:${String.format("%02d", selectedMinute)}?")
                setPositiveButton("Yes") { _, _ ->
                    setAlarmForPackage(packageName, calendar.timeInMillis)
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }, hour, minute, true).show()
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

    private fun setAlarmForPackage(packageName: String, alarmTime: Long) {
        // Set alarm for the package
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        Intent(this, AlarmReceiver::class.java).let { intent ->
            val uniqueRequestCode = System.currentTimeMillis().hashCode()
            intent.putExtra("packageName", packageName)
            intent.putExtra("alarmID", uniqueRequestCode)

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
            installedAppViewModel.saveAlarm(uniqueRequestCode ,packageName, alarmTime)
            Log.d(TAG, "setAlarmForPackage: $packageName RequestCode: $uniqueRequestCode")

            installedAppViewModel.getInstalledApps()

        }

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        installedAppViewModel.getInstalledApps()
    }


}
