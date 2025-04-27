package com.example.androidappscheduler.adapters

import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.entries.AlarmLauncher
import java.util.Locale
import android.icu.text.SimpleDateFormat
import android.widget.ImageButton

class AlarmsGridAdapter(private val items: List<AlarmLauncher>,private val onDeleteClick: (Int, String)-> Unit, private val onEditClick: (Int)-> Unit = {}) : RecyclerView.Adapter<AlarmsGridAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_events_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeInMillis = items[position].launchTime
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timeInMillis)
        holder.tvAlarmTime.text = time
        holder.tvAlarmLaunchStatus.text = if (items[position].isLaunched) "Launched" else "Not Launched"
        if(items[position].isLaunched) {
            holder.ibtnEdit.visibility = View.GONE
        } else {
            holder.ibtnEdit.visibility = View.VISIBLE
        }

        holder.ibtnDelete.setOnClickListener {
            onDeleteClick(items[position].alarmId, items[position].packageName)
        }

        holder.ibtnEdit.setOnClickListener {
            onEditClick(items[position].alarmId)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlarmTime: TextView = itemView.findViewById(R.id.tv_alarm_time_adapter)
        val tvAlarmLaunchStatus: TextView = itemView.findViewById(R.id.tv_alarm_launch_adapter)
        val ibtnEdit: ImageButton = itemView.findViewById(R.id.ib_edit_grid_adapter)
        val ibtnDelete: ImageButton = itemView.findViewById(R.id.ib_delete_grid_adapter)
    }
}
