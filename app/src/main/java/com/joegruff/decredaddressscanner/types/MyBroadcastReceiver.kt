package com.joegruff.decredaddressscanner.types

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.joegruff.decredaddressscanner.R
import com.joegruff.decredaddressscanner.activities.MainActivity
import com.joegruff.decredaddressscanner.activities.ViewAddressActivity
import com.joegruff.decredaddressscanner.viewfragments.ViewAddressFragment
import java.util.*
import kotlin.collections.ArrayList

fun setRepeatingAlarm(ctx: Context, startInterval: Long) {
    val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent =
        Intent(ctx.applicationContext, MyBroadcastReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(ctx, 0, intent, 0)
        }

    alarmMgr.setInexactRepeating(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + startInterval,
        AlarmManager.INTERVAL_HALF_HOUR,
        alarmIntent
    )
}

const val CHANNEL_ID = "com.joegruff.decredaddressscanner.notification_channel"
const val NOTIFICATION_ID = 1337

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver() {
    private val changedAddressObjects = ArrayList<Address>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
                setRepeatingAlarm(context, 1000 * 60)
                return
            }
            createNotificationChannel(context)


            // Check for starred addresses and whether the current address is being displayed on screen.
            var numStarredAddresses = 0L
            for (starredAddress in addrBook(context).addresses.filter { it.isBeingWatched }
                .filter { !it.balanceSwirlIsShown() }) {
                starredAddress.delegates[1] = this
                starredAddress.update(context)
                numStarredAddresses += 1
            }


            // Give it five seconds to find changed addresses, report results as alert if something changed.
            Handler(Looper.getMainLooper()).postDelayed({
                val message: String
                val size = changedAddressObjects.size
                val myPendingIntent: PendingIntent?

                when {
                    size < 1 -> {
                        return@postDelayed
                    }
                    size < 2 -> {
                        val addr = changedAddressObjects[0]
                        var title = addr.title
                        if (title == "")
                            title = addr.address
                        val amountString = addr.amount.toString()
                        val oldBalance = addr.amountOld.toString()
                        val formattedAmountString = setAmounts(amountString, oldBalance)
                        message = context.getString(
                            R.string.changed_amounts_one,
                            title,
                            formattedAmountString
                        )
                        val myNotificationIntent = Intent(context, ViewAddressActivity::class.java)
                        myNotificationIntent.putExtra(
                            ViewAddressFragment.INTENT_ADDRESS_DATA,
                            addr.address
                        )
                        myNotificationIntent.flags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        myPendingIntent = TaskStackBuilder.create(context).run {
                            addNextIntentWithParentStack(myNotificationIntent)
                            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                        }
                    }
                    else -> {
                        val myNotificationIntent = Intent(context, MainActivity::class.java)
                        myNotificationIntent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        myPendingIntent =
                            PendingIntent.getActivity(context, 0, myNotificationIntent, 0)
                        message = context.getString(R.string.changed_amounts_many)
                    }
                }
                val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_notification2)
                    .setContentTitle(context.getString(R.string.changed_amounts_notification_title))
                    .setContentText(message)
                    .setContentIntent(myPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(NOTIFICATION_ID, mBuilder.build())

            }, (4500 + 1000 * numStarredAddresses))

        }
    }

    private fun createNotificationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.getString(R.string.channel_name)
            val description = ctx.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAmounts(balance: String, oldBalance: String): String {
        val difference = balance.toDouble() - oldBalance.toDouble()
        return when {
            difference > 0.0 -> {
                "+" + abbreviatedAmountFromString(difference.toString())
            }
            difference < 0.0 -> {
                abbreviatedAmountFromString(difference.toString())
            }
            else -> "0"
        }
    }

    override fun processBegan() {}

    override fun processError(str: String) {}

    override fun processFinished(addr: Address, ctx: Context) {
        // Catch all changed starred addresses.
        if (addr.amount != addr.amountOld && Date().time - addr.timestampChange < 1000 * 10)
            changedAddressObjects.add(addr)
    }
}
