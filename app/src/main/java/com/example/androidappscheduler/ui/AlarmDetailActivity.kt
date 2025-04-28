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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidappscheduler.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.adapters.AlarmsGridAdapter
import com.example.androidappscheduler.utils.Constants.openAlarmDialog
import com.example.androidappscheduler.viewmodels.AlarmDetailViewModel
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "AlarmDetailActivity"

@AndroidEntryPoint
class AlarmDetailActivity : AppCompatActivity() {

    lateinit var alarms_rv: RecyclerView
    private val alarmDetailViewModel: AlarmDetailViewModel by viewModels()
    lateinit var tvAppName: TextView
    lateinit var tvPackageName: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
