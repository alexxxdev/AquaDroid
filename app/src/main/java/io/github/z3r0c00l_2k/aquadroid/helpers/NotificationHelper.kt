package io.github.z3r0c00l_2k.aquadroid.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import io.github.z3r0c00l_2k.aquadroid.R
import io.github.z3r0c00l_2k.aquadroid.utils.AppUtils
import java.util.*

class NotificationHelper(val ctx: Context) {
    private var notificationManager: NotificationManager? = null

    private val CHANNEL_ONE_ID = "io.github.z3r0c00l_2k.aquadroid.CHANNELONE"
    private val CHANNEL_ONE_NAME = "Channel One"


    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val prefs = ctx.getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)
            val notificationsNewMessageRingtone = prefs.getString(
                AppUtils.NOTIFICATION_TONE_URI_KEY, RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                ).toString()
            )
            val notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            if (notificationsNewMessageRingtone!!.isNotEmpty()) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                notificationChannel.setSound(Uri.parse(notificationsNewMessageRingtone), audioAttributes)
            }

            getManager()!!.createNotificationChannel(notificationChannel)
        }
    }

    fun getNotification(
        title: String,
        body: String,
        notificationsTone: String?
    ): Notification.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Notification.Builder(ctx.applicationContext, CHANNEL_ONE_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setColorized(true)
                .setColor(Color.BLUE)
                .setShowWhen(true)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        ctx.resources,
                        R.mipmap.ic_launcher
                    )
                )
                .setSmallIcon(R.drawable.ic_small_logo)
                .setAutoCancel(true)
        } else {

            val notification = Notification.Builder(ctx.applicationContext)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        ctx.resources,
                        R.mipmap.ic_launcher
                    )
                )
                .setSmallIcon(R.drawable.ic_small_logo)
                .setAutoCancel(true)

            notification.setShowWhen(true)

            notification.setSound(Uri.parse(notificationsTone))

            return notification
        }
    }

    private fun shallNotify(): Boolean {
        val prefs = ctx.getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)

        val notificationsNewMessage = prefs.getBoolean("notifications_new_message", true)
        var doNotDisturbOff = true

        val startTimestamp = prefs.getLong("pref_notification_start", 0)
        val stopTimestamp = prefs.getLong("pref_notification_stop", 0)

        if (startTimestamp > 0 && stopTimestamp > 0) {
            val now = Calendar.getInstance().time

            val start = Date(startTimestamp)
            val stop = Date(stopTimestamp)

            doNotDisturbOff = compareTimes(now, start) >= 0 && compareTimes(now, stop) <= 0
        }

        return notificationsNewMessage && doNotDisturbOff
    }

    /* Thanks to:
     * https://stackoverflow.com/questions/7676149/compare-only-the-time-portion-of-two-dates-ignoring-the-date-part
    */
    private fun compareTimes(currentTime: Date, timeToRun: Date): Long {
        val currentCal = Calendar.getInstance()
        currentCal.time = currentTime

        val runCal = Calendar.getInstance()
        runCal.time = timeToRun
        runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH))
        runCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH))
        runCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR))

        return currentCal.timeInMillis - runCal.timeInMillis
    }

    fun notify(id: Long, notification: Notification.Builder) {
        if (shallNotify()) {
            getManager()!!.notify(id.toInt(), notification.build())
        } else {
            Log.i("WateryDroid", "dnd period")
        }
    }

    private fun getManager(): NotificationManager? {
        if (notificationManager == null) {
            notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager
    }
}