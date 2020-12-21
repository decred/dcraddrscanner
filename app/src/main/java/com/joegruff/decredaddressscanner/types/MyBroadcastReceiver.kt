package com.joegruff.decredaddressscanner.types

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.joegruff.decredaddressscanner.R
import com.joegruff.decredaddressscanner.activities.MainActivity
import com.joegruff.decredaddressscanner.activities.ViewAddressActivity
import com.joegruff.decredaddressscanner.viewfragments.ViewAddressFragment
import org.json.JSONObject
import org.json.JSONTokener
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


    private val changedAddressObjects = ArrayList<AddressObject>()
    private var numStarredAddresses = 0L

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            Log.d("broadcast receiver", "intent is: " + intent?.action)
            if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
                setRepeatingAlarm(context, 1000 * 60)
                return
            }

            createNotificationChannel(context)

            AddressBook.fillAddressBook(context)

            // Check for starred addresses and whether the current address is being displayed on screen.
            for (starredAddress in AddressBook.addresses.filter { it.isBeingWatched }
                .filter { !balanceSwirlNotNull() }) {
                starredAddress.delegates[1] = this
                starredAddress.update(false)
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
                        val token = changedAddressObjects[0]
                        var title = token.title
                        val address = token.address
                        if (title == "")
                            title = address
                        val amountString = token.amount.toString()
                        val oldBalance = token.amountOld.toString()
                        val formattedAmountString = setAmounts(amountString, oldBalance)
                        message = context.getString(
                            R.string.changed_amounts_one,
                            title,
                            formattedAmountString
                        )
                        val myNotificationIntent = Intent(context, ViewAddressActivity::class.java)
                        myNotificationIntent.putExtra(
                            ViewAddressFragment.INTENT_ADDRESS_DATA,
                            address
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

                // These addresses should be holding a reference to addressBook, so should update properly.
                AddressBook.saveAddressBook(context)

            }, (1000 + 4500 * numStarredAddresses))

        }
    }

    private fun createNotificationChannel(ctx: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.getString(R.string.channel_name)
            val description = ctx.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun setAmounts(balance: String, oldBalance: String): String {
        val difference = balance.toDouble() - oldBalance.toDouble()
        return when {
            difference > 0.0 -> {
                "+" + amountFromString(difference.toString())
            }
            difference < 0.0 -> {
                amountFromString(difference.toString())
            }
            else -> "0"
        }
    }

    private fun amountFromString(amountString: String): String {
        return AddressBook.abbreviatedAmountFromString(amountString)
    }

    override fun processBegan() {
        return
    }

    override fun processFinished(output: String) {
        // Catch all changed starred addresses.
        if (output != "" && output != NO_CONNECTION) {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val address = token.getString(JSON_ADDRESS)
                val addressObject = AddressBook.getAddressObject(address)
                val amount = addressObject.amount
                val oldBalance = addressObject.amountOld
                val timestamp = addressObject.timestampChange
                if (amount != oldBalance && Date().time - timestamp < 1000 * 10)
                    changedAddressObjects.add(addressObject)
            }
        }
    }
}
