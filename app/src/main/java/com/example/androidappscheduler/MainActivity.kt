package com.example.androidappscheduler

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.adapters.InstalledPackageAdapter

class MainActivity : AppCompatActivity() {
    lateinit var installedPackageAdapter: InstalledPackageAdapter
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        installedPackageAdapter = InstalledPackageAdapter(this, emptyList())

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        getAllinstalledPackages().let {
            for (packageName in it) {
                println("Installed package: $packageName")
            }
            installedPackageAdapter = InstalledPackageAdapter(this, it)
            recyclerView.adapter = installedPackageAdapter
            installedPackageAdapter.notifyDataSetChanged()
        }
    }

    fun getAllinstalledPackages(): List<String> {
        val packageManager = packageManager
        val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        val packageNames = packages.filter {
            packageManager.getLaunchIntentForPackage(it.packageName) != null
        }.map { it.packageName }
        return packageNames
    }
}