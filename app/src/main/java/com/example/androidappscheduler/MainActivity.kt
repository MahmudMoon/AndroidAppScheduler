package com.example.androidappscheduler

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.adapters.InstalledPackageAdapter

private const val TAG = "MainActivity"

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

        installedPackageAdapter = InstalledPackageAdapter(this, emptyList()) { packageName ->
            launchApp(packageName)
        }

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        getAllinstalledPackages().let {
            for (packageName in it) {
                println("Installed package: $packageName")
            }
            installedPackageAdapter = InstalledPackageAdapter(this, it) { packageName ->
                launchApp(packageName)
            }
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

    fun launchApp(packageName: String) {
        val packageManager: PackageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        Log.d(TAG, "launchApp: $packageName")
        Log.d(TAG, "launchApp: $launchIntent")
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            // The application does not exist or cannot be launched directly.
            // Optionally, take the user to the Google Play Store to install the app.
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "market://details?id=$packageName"
                        )
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(
                            "https://play.google.com/store/apps/details?id=$packageName"
                        )
                    )
                )
            }
        }
    }
}