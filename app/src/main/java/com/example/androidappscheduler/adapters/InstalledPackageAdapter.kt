package com.example.androidappscheduler.adapters

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.models.PackageInstance

class InstalledPackageAdapter(
    private val context: Context,
    private val packageInstances: List<PackageInstance>,
    private val alarmDetailClicked: (String) -> Unit = {},
    private val packageClicked: (String) -> Unit,
) : RecyclerView.Adapter<InstalledPackageAdapter.PackageViewHolder>() {

    override fun onCreateViewHolder(
        parent: android.view.ViewGroup,
        viewType: Int
    ): PackageViewHolder {
        val view = android.view.LayoutInflater.from(context)
            .inflate(R.layout.installed_app_info_item, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.packageNameTextView.text = packageInstances[position].appName
        holder.packageNameTextView.setOnClickListener {
            packageClicked(packageInstances[position].packageName)
        }
        var color = 0;
        if(packageInstances[position].alarms.isNotEmpty()) {
            holder.alarmImage.visibility = View.VISIBLE
            color = "#056b11".toColorInt()
        } else {
            holder.alarmImage.visibility = View.INVISIBLE
            color = "#000000".toColorInt()
        }
        holder.packageNameTextView.setTextColor(color)

        holder.rightArrow.setOnClickListener {
            alarmDetailClicked(packageInstances[position].packageName)
        }
    }

    override fun getItemCount(): Int {
        return packageInstances.size
    }


    class PackageViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val packageNameTextView: TextView = itemView.findViewById(R.id.app_name)
        val alarmImage : ImageView = itemView.findViewById(R.id.iv_alarm_clock)
        val rightArrow : ImageButton = itemView.findViewById(R.id.ib_right_arrow)
    }
}