package com.example.androidappscheduler.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.androidappscheduler.R
import com.example.androidappscheduler.dao.AlarmLauncherDao
import com.example.androidappscheduler.data.InstalledAppRepository
import com.example.androidappscheduler.utils.Constants
import com.example.androidappscheduler.utils.Constants.getFromSP
import com.example.androidappscheduler.utils.Constants.getLastNotificationID
import com.example.androidappscheduler.utils.Constants.saveInSP
import com.example.androidappscheduler.utils.Constants.saveLastNotificationID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "LauncherForegroundService"

@AndroidEntryPoint
class LauncherForegroundService: Service() {

    @Inject
    lateinit var notificationManager: NotificationManager;

    @Inject
    lateinit var _packageManager: PackageManager;

    @Inject
    lateinit var installedAppRepo: InstalledAppRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val packageName = intent?.getStringExtra("packageName") ?: ""
        Log.d(TAG, "onStartCommand: Package Name: $packageName")

        val alarmID = intent?.getIntExtra("alarmID", 0) ?: 0
        Log.d(TAG, "onStartCommand: Alarm ID: $alarmID")

        var appName = packageName

        try {
            val applicationInfo = _packageManager.getApplicationInfo(packageName, 0)
            appName = _packageManager.getApplicationLabel(applicationInfo).toString()

        }catch (nameException: PackageManager.NameNotFoundException) {
            Log.e(TAG, "onStartCommand: "+nameException.printStackTrace())
        }

        val notification: Notification? = createNotification(appName)
        Log.d(TAG, "onStartCommand:Create notification for ${appName}:= "+packageName.hashCode())
        startForeground(abs(packageName.hashCode()), notification)
        saveLastNotificationID(applicationContext, abs(packageName.hashCode()))

        if (packageName.isNotEmpty()) {

            var randomDelay = System.currentTimeMillis() + 10000;
            val handler = Handler(Looper.getMainLooper())

            val lastLaunchTime = getFromSP(applicationContext);

            randomDelay = maxOf(randomDelay, lastLaunchTime + 10000)

            saveInSP(applicationContext, randomDelay);


            Log.d(TAG, "onReceive: Current Time" + System.currentTimeMillis())
            Log.d(TAG, "onReceive: Scheduled Time: " + randomDelay);
            Log.d(TAG, "onReceive: Last launched Scheduled " + lastLaunchTime)
            Log.d(TAG, "onReceive: =============================================")
            Log.d(
                TAG,
                "onReceive: Diffence from now: ${(randomDelay - System.currentTimeMillis()) / 1000}"
            )
            handler.postDelayed({
                launchApp(applicationContext, packageName)

                CoroutineScope(Dispatchers.IO).launch {
                   installedAppRepo.markAlarmAsLaunched(alarmID)
                }

                if(getLastNotificationID(applicationContext) == abs(packageName.hashCode())){
                    stopForegroundService(abs(packageName.hashCode()))
                }
            }, randomDelay - System.currentTimeMillis())

        } else
            Log.d(TAG, "onReceive: Package Name is empty")


        return START_STICKY
    }

    private fun launchApp(context: Context, packageName: String) {
        val launchIntent = _packageManager.getLaunchIntentForPackage(packageName)
        Log.d(TAG, "launchApp: $packageName")
        //Log.d(TAG, "launchApp: $launchIntent")
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "Application not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
    }

    private fun createNotification(appName: String): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(Constants.APP_NAME)
            .setContentText("Launching Apps")
            .setSmallIcon(R.drawable.baseline_android_24)
        return builder.build()
    }

    private fun stopForegroundService(notificationId: Int) {
        notificationManager.cancel(notificationId)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "stopping : $notificationId")
    }
}