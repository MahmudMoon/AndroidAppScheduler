package com.example.androidappscheduler.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidappscheduler.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.adapters.AlarmsGridAdapter
import com.example.androidappscheduler.utils.Constants.openAlarmDialog
import com.example.androidappscheduler.viewmodels.AlarmDetailViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "AlarmDetailActivity"

@AndroidEntryPoint
class AlarmDetailActivity : AppCompatActivity() {

    lateinit var alarms_rv: RecyclerView
    private val alarmDetailViewModel: AlarmDetailViewModel by viewModels()
    lateinit var tvAppName: TextView
    lateinit var tvPackageName: TextView
    lateinit var fabAddAlarm: FloatingActionButton
    lateinit var toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_detail)

        toolbar = findViewById(R.id.toolbar_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Scheduled Alarms"

        fabAddAlarm = findViewById(R.id.fab_add_alarm_alarm_detail)
        tvAppName = findViewById(R.id.tv_app_name_detail)
        tvPackageName = findViewById(R.id.tv_package_name_detail)
        val packageName = intent.getStringExtra("packageName")
        tvPackageName.text = packageName
        tvAppName.text = alarmDetailViewModel.getAppName(packageName ?: "")

        alarms_rv = findViewById<RecyclerView>(R.id.recycler_view)
        alarms_rv.layoutManager = GridLayoutManager(this, 2) // 2 columns
        alarms_rv.adapter =
            AlarmsGridAdapter(emptyList(), onDeleteClick = { _, _ -> }, onEditClick = { _, _ -> })

        alarmDetailViewModel.alarmDeleted.observe(this) {
            Log.d(TAG, "onCreate: AlarmDeleted ${it}")
            if (it) {
                alarmDetailViewModel.getAlarmList(tvPackageName.text.toString())
            }
        }

        alarmDetailViewModel.alarmUpdated.observe(this) {
            Log.d(TAG, "onCreate: AlarmUpdated ${it}")
            if (it) {
                alarmDetailViewModel.getAlarmList(tvPackageName.text.toString())
            }
        }

        alarmDetailViewModel.alarmListDataForDetail.observe(this) { alarmList ->
            if (alarmList.isNotEmpty()) {
                alarms_rv.adapter =
                    AlarmsGridAdapter(alarmList.sortedBy { it.launchTime }.reversed(),
                        onDeleteClick = { alarmId, packageName ->
                            Log.d(TAG, "onCreate: OnDelete click for id ${alarmId}")
                            showConfirmationDialog(
                                "Delete Alarm",
                                "Are you sure you want to delete this alarm?",
                                this
                            ) {
                                Log.d(TAG, "onCreate: OnDelete click for id ${alarmId} confirmed")
                                alarmDetailViewModel.deleteAlarm(alarmId, this, packageName)
                                Toast.makeText(
                                    this,
                                    "Alarm with ID $alarmId deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onEditClick = { alarmId, launchTime ->
                            Log.d(
                                TAG,
                                "onCreate: OnEdit click for id $alarmId , launchTime $launchTime"
                            )
                            openAlarmDialog(
                                packageName = tvPackageName.text.toString(),
                                suggestedTime = launchTime,
                                context = this
                            ) { packName, alarmTime ->
                                Log.d(TAG, "onCreate: editAlarmForPack: $packName, $alarmTime")
                                alarmDetailViewModel.editAlarm(
                                    context = this@AlarmDetailActivity,
                                    alarmId = alarmId,
                                    alarmTime = alarmTime,
                                    packageName = packName
                                )
                            }
                            Toast.makeText(
                                this,
                                "Alarm with ID $alarmId edited",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
            } else {
                alarms_rv.adapter = AlarmsGridAdapter(
                    emptyList(),
                    onDeleteClick = { _, _ -> },
                    onEditClick = { _, _ -> })
                Toast.makeText(this, "No alarms found for this app", Toast.LENGTH_SHORT).show()
            }
            alarms_rv.adapter?.notifyDataSetChanged()
        }

        alarmDetailViewModel.successfullyStoredAlarm.observe(this){
            Log.d(TAG, "onCreate: Successfully added alarm: $it")
            if (it) {
                alarmDetailViewModel.getAlarmList(tvPackageName.text.toString())
                Toast.makeText(this, "Alarm added successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add alarm", Toast.LENGTH_SHORT).show()
            }
        }

        fabAddAlarm.setOnClickListener {
            openAlarmDialog(
                packageName = tvPackageName.text.toString(),
                context = this
            ) { packName, alarmTime ->
                Log.d(TAG, "onCreate: setAlarmForPackage: $packName, $alarmTime")
                setAlarmForPackage(
                    packageName = packName,
                    alarmTime = alarmTime
                )
            }
        }
    }

    private fun setAlarmForPackage(packageName: String, alarmTime: Long) {
        val uniqueRequestCode = System.currentTimeMillis().hashCode()
        alarmDetailViewModel.saveAlarm(this@AlarmDetailActivity, uniqueRequestCode, packageName, alarmTime)
        Log.d(TAG, "setAlarmForPackage: $packageName RequestCode: $uniqueRequestCode")
       // mainActivityViewModel.getInstalledApps()
    }

    override fun onStart() {
        super.onStart()
        alarmDetailViewModel.getAlarmList(tvPackageName.text.toString())
    }
}

private fun showConfirmationDialog(
    title: String,
    message: String,
    context: Context,
    onConfirmation: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { dialog, _ ->
            onConfirmation()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        .show()

}
