package ai.laennec.pavakka.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

// All local-reminder plumbing in one place: channel, receiver, and scheduler.
// Uses AlarmManager + a BroadcastReceiver — no extra libraries, no backend.

private const val CHANNEL_ID = "pavakka_reminders"

object Reminders {
    // Request codes so each alarm has a stable PendingIntent.
    private const val RC_WATER = 1000
    private const val RC_BREAKFAST = 1001
    private const val RC_LUNCH = 1002
    private const val RC_DINNER = 1003
    private const val RC_FAST = 1004

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java)
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                        .apply { description = "Hydration, meal logging and fasting reminders" }
                )
            }
        }
    }

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pending(context: Context, rc: Int, title: String, body: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("rc", rc)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, rc, intent, flags)
    }

    // --- Water: inexact repeating every N hours, anchored to the next :00 ---
    fun scheduleWater(context: Context, intervalHours: Int) {
        ensureChannel(context)
        val first = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        alarmManager(context).setInexactRepeating(
            AlarmManager.RTC_WAKEUP, first, intervalHours * 3600_000L,
            pending(context, RC_WATER, "💧 Time to hydrate", "Drink a glass of water and log it in Pavakka.")
        )
    }

    fun cancelWater(context: Context) =
        alarmManager(context).cancel(pending(context, RC_WATER, "", ""))

    // --- Meals: daily at fixed clock times ---
    fun scheduleMeals(context: Context) {
        ensureChannel(context)
        dailyAt(context, RC_BREAKFAST, 9, 0, "🌅 Log your breakfast", "Don't forget to track your morning meal.")
        dailyAt(context, RC_LUNCH, 13, 30, "☀️ Log your lunch", "Keep your diary up to date.")
        dailyAt(context, RC_DINNER, 20, 0, "🌙 Log your dinner", "Round off your day — log dinner.")
    }

    fun cancelMeals(context: Context) {
        listOf(RC_BREAKFAST, RC_LUNCH, RC_DINNER).forEach {
            alarmManager(context).cancel(pending(context, it, "", ""))
        }
    }

    private fun dailyAt(context: Context, rc: Int, hour: Int, minute: Int, title: String, body: String) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarmManager(context).setInexactRepeating(
            AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY,
            pending(context, rc, title, body)
        )
    }

    // --- Fasting: one-shot when the goal is reached ---
    fun scheduleFastComplete(context: Context, targetHours: Int) {
        ensureChannel(context)
        val at = System.currentTimeMillis() + targetHours * 3600_000L
        val pi = pending(context, RC_FAST, "🎉 Fast complete!", "You hit your ${targetHours}h fasting goal. Great job!")
        alarmManager(context).set(AlarmManager.RTC_WAKEUP, at, pi)
    }

    fun cancelFastComplete(context: Context) =
        alarmManager(context).cancel(pending(context, RC_FAST, "", ""))
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.ensureChannel(context)
        val title = intent.getStringExtra("title") ?: "Pavakka"
        val body = intent.getStringExtra("body") ?: ""
        val rc = intent.getIntExtra("rc", 0)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            androidx.core.app.NotificationManagerCompat.from(context).notify(rc, notification)
        } catch (_: SecurityException) { /* permission not granted */ }
    }
}
