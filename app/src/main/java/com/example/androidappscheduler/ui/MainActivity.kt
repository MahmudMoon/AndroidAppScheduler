package com.example.androidappscheduler.ui

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.adapters.InstalledPackageAdapter
import com.example.androidappscheduler.utils.Constants.openAlarmDialog
import com.example.androidappscheduler.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var installedPackageAdapter: InstalledPackageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    @Inject
    lateinit var alarmManager: AlarmManager

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Installed Apps"

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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 104) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission granted 104")
                if (requestForPermissions()) {
                    mainActivityViewModel.getInstalledApps()
                }
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permission denied 104")
                // requestForPermissions()
            }
        } else if (requestCode == 105) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission granted 105")
                if (requestForPermissions()) {
                    mainActivityViewModel.getInstalledApps()
                }
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permission denied 105")
                // requestForPermissions()
            }
        }
    }

    private fun requestPermissionAskingDialog(title: String, message: String, intent: Intent? = null, task: ()->Unit = {}) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                if(intent!=null)
                    startActivity(intent)
                else
                    task()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                alertUserRegardingPermission(intent, task)
            }
            .setOnDismissListener {
            }.show()

    }

    private fun alertUserRegardingPermission(intent: Intent?, task: () -> Unit = {}) {
        AlertDialog.Builder(this)
            .setTitle("Alert !!")
            .setMessage("Please allow permissions to continue this app")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if(intent!=null)
                    startActivity(intent)
                else
                    task()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {

            }.show()
    }

    private fun requestForPermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(
                    TAG,
                    "requestForPermissions: CHECKING canScheduleExactAlarms: ${alarmManager.canScheduleExactAlarms()}"
                )

                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                requestPermissionAskingDialog(
                    "Alert !!",
                    "Please allow permission to set alarm",
                    intent
                )
                return false
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!Settings.canDrawOverlays(this)) {
                    Log.d(
                        TAG,
                        "requestForPermissions: Settings canDrawOverlays: ${
                            Settings.canDrawOverlays(this)
                        }"
                    )

                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        ("package:$packageName").toUri()
                    )

                    requestPermissionAskingDialog(
                        "Alert !!",
                        "Please allow Overlay permission open app from background",
                        intent
                    )
                    return false
                }
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(
                    TAG,
                    "requestForPermissions: requestForPermissions: 104 SCHEDULE EXACT ALARM"
                )
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                    104
                )
                return false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(
                    TAG,
                    "requestForPermissions: requestForPermissions:  105 POST NOTIFICATION"
                )

                requestPermissionAskingDialog("Alert !!",
                    "Please allow permission to show notification",
                    null,
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        105
                    )
                }
                return false
            }
        }
        Log.d(TAG, "requestForPermissions: ALL permissions granted")
        return true
    }

    private fun setAlarmForPackage(packageName: String, alarmTime: Long) {
        val uniqueRequestCode = abs(System.currentTimeMillis().hashCode())
        mainActivityViewModel.saveAlarm(
            this@MainActivity,
            uniqueRequestCode,
            packageName,
            alarmTime
        )
        Log.d(TAG, "setAlarmForPackage: $packageName RequestCode: $uniqueRequestCode")
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        if (requestForPermissions()) {
            mainActivityViewModel.getInstalledApps()
        }
    }
}
