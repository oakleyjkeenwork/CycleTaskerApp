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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.oakley.cycletasker.data.AppState
import com.oakley.cycletasker.data.CycleTaskerRepository
import com.oakley.cycletasker.domain.ScheduleEngine
import java.time.LocalDate

class CompletionNotificationService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshForegroundNotification()
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        CycleTaskerNotifications.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == CycleTaskerNotifications.ActionStop) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        refreshForegroundNotification()
        handler.removeCallbacks(refreshRunnable)
        handler.postDelayed(refreshRunnable, 15_000)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(refreshRunnable)
        super.onDestroy()
    }

    private fun refreshForegroundNotification() {
        val state = CycleTaskerRepository(filesDir).load()
        if (!state.settings.notificationsEnabled || !CycleTaskerNotifications.hasPermission(this)) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        startForeground(
            CycleTaskerNotifications.NotificationId,
            CycleTaskerNotifications.buildNotification(this, state)
        )
    }
}

object CycleTaskerNotifications {
    const val ChannelId = "todays_completion"
    const val NotificationId = 101
    const val ActionStop = "com.oakley.cycletasker.STOP_COMPLETION_NOTIFICATION"

    fun startOrUpdate(context: Context) {
        if (!hasPermission(context)) return
        val state = CycleTaskerRepository(context.filesDir).load()
        if (!state.settings.notificationsEnabled) return
        createChannel(context)
        ContextCompat.startForegroundService(
            context,
            Intent(context, CompletionNotificationService::class.java)
        )
    }

    fun stop(context: Context) {
        context.startService(
            Intent(context, CompletionNotificationService::class.java).apply {
                action = ActionStop
            }
        )
        NotificationManagerCompat.from(context).cancel(NotificationId)
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
            .setSmallIcon(R.drawable.ic_notification)
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
