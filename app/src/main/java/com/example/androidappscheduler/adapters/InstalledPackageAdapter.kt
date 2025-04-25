package com.example.androidappscheduler.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R


private const val TAG = "InstalledPackageAdapter"

class InstalledPackageAdapter(
    private val context: Context,
    private val packageList: List<String>,
    private val packageClicked: (String) -> Unit
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
        holder.packageNameTextView.text = packageList[position]
        holder.itemView.setOnClickListener {
            packageClicked(packageList[position])
            //launchApp(packageList[position])
        }
    }

    override fun getItemCount(): Int {
        return packageList.size
    }


    class PackageViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val packageNameTextView: TextView = itemView.findViewById(R.id.app_name)
    }
}