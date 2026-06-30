package com.oakley.cycletasker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.oakley.cycletasker.data.AppState
import com.oakley.cycletasker.data.CycleTaskerRepository
import com.oakley.cycletasker.domain.ScheduleEngine
import java.time.LocalDate

class CompletionNotificationService : Service() {
    override fun onCreate() {
        super.onCreate()
        CycleTaskerNotifications.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = CycleTaskerRepository(filesDir).load()
        startForeground(
            CycleTaskerNotifications.NotificationId,
            CycleTaskerNotifications.buildNotification(this, state)
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

object CycleTaskerNotifications {
    const val ChannelId = "todays_completion"
    const val NotificationId = 101

    fun startOrUpdate(context: Context) {
        if (!hasPermission(context)) return
        createChannel(context)
        ContextCompat.startForegroundService(
            context,
            Intent(context, CompletionNotificationService::class.java)
        )
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ChannelId,
            "Today's completion",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows today's CycleTasker completion percentage."
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context, state: AppState): Notification {
        val today = LocalDate.now()
        val tasks = ScheduleEngine.scheduledTasksForDate(
            state.individualTasks,
            state.cycleRoutines,
            state.completionHistory,
            today
        )
        val percent = ScheduleEngine.completionPercent(tasks)
        val completed = tasks.count { it.completed }
        val total = tasks.size
        val content = if (total == 0) {
            "No tasks due today"
        } else {
            "$percent% complete ($completed/$total)"
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("TODAY'S COMPLETION")
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    fun hasPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }
}
