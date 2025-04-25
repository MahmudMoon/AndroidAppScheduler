package com.example.androidappscheduler.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidappscheduler.R
import com.example.androidappscheduler.adapters.InstalledPackageAdapter
import com.example.androidappscheduler.viewmodels.InstalledAppViewModel
import dagger.hilt.android.AndroidEntryPoint
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

        installedPackageAdapter = InstalledPackageAdapter(this, emptyList()) { packageName ->
            launchApp(packageName)
        }

        recyclerView = findViewById(R.id.installed_apps_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = installedPackageAdapter


        installedAppViewModel.installedAppListState.value.let {
            Log.d(TAG, "onCreate: $it")
            installedPackageAdapter = InstalledPackageAdapter(this, it) { packageName ->
                launchApp(packageName)
            }
            recyclerView.adapter = installedPackageAdapter
            installedPackageAdapter.notifyDataSetChanged()
        }

    }

    private fun launchApp(packageName: String) {
        val packageManager: PackageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        Log.d(TAG, "launchApp: $packageName")
        Log.d(TAG, "launchApp: $launchIntent")
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Application not found", Toast.LENGTH_SHORT).show()
        }
    }
}