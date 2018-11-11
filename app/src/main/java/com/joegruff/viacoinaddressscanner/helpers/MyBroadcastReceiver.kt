package com.joegruff.viacoinaddressscanner.helpers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import com.joegruff.viacoinaddressscanner.MainActivity
import com.joegruff.viacoinaddressscanner.R
import com.joegruff.viacoinaddressscanner.ViewAddressFragment
import org.json.JSONObject
import org.json.JSONTokener
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import java.util.*
import kotlin.collections.ArrayList

fun setrepeatingalarm(ctx: Context, startInterval : Long) {
    val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(ctx.applicationContext, MyBroadcastReceiver::class.java).let { intent ->
        PendingIntent.getBroadcast(ctx, 0, intent, 0)
    }

    alarmMgr.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + startInterval,
            AlarmManager.INTERVAL_HALF_HOUR,
            alarmIntent
    )

}

const val CHANNEL_ID = "com.joegruff.viacoinaddressscanner.notification_channel"
const val NOTIFICATION_ID = 1337

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver() {


    val changedAddressObjects = ArrayList<AddressObject>()
    var numStarredAddresses = 0L

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            Log.d("broadcast receiver", "intent is: " + intent?.action)
            if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
                setrepeatingalarm(context, 1000 * 60)
                return
            }


            createNotificationChannel(context)

            AddressBook.fillAddressBook(context)

            //check for starred addresses and wether the current address is being displayed on screen
            for (starredAddress in AddressBook.addresses.filter { it.isBeingWatched }.filter { !balanceSwirlNotNull() }) {
                starredAddress.delegates[1] = this
                starredAddress.update(false)
                numStarredAddresses += 1
                Log.d("mybroadcastreceiver", "onreceive fired " + starredAddress.address)
            }


            //give it five seconds to find changed addresses, report results as alert if something changed
            Handler().postDelayed({

                var message = ""
                val size = changedAddressObjects.size
                var myPendingIntent: PendingIntent? = null

                if (size < 1) {
                    return@postDelayed
                } else if (size < 2) {

                    val token = changedAddressObjects[0]
                    var title = token.title
                    val address = token.address
                    if (title.equals(""))
                        title = address
                    val amountString = token.amount.toString()
                    val oldBalance = token.amountOld.toString()

                    val formattedAmountString = setAmounts(amountString, oldBalance)


                    message = context.getString(R.string.changed_amounts_one, title, formattedAmountString)
                    val myNotificationIntent = Intent(context, ViewAddressActivity::class.java)
                    myNotificationIntent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                    myNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    myPendingIntent = TaskStackBuilder.create(context).run {
                        addNextIntentWithParentStack(myNotificationIntent)
                        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                    }


                } else {

                    val myNotificationIntent = Intent(context, MainActivity::class.java)
                    myNotificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    myPendingIntent = PendingIntent.getActivity(context, 0, myNotificationIntent, 0)
                    message = context.getString(R.string.changed_amounts_many)

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

                //these addresses should be holding a reference to addressBook, so should update properly, but im not sure
                AddressBook.saveAddressBook(context)


            }, (100 * 25 * numStarredAddresses)  )

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


    fun setAmounts(balance: String, oldBalance: String): String {
        val difference = balance.toDouble() - oldBalance.toDouble()

        val differenceText =
                when {
                    difference > 0.0 -> {
                        "+" + amountfromstring(difference.toString())
                    }
                    difference < 0.0 -> {
                        amountfromstring(difference.toString())
                    }
                    else -> "0"
                }

        return differenceText
    }

    fun amountfromstring(amountString: String): String {
        return AddressBook.abbreviatedAmountfromstring(amountString)
    }

    override fun processbegan() {
        Log.d("mybroadcastreceiver", "process began")
        return
    }

    override fun processfinished(output: String?) {
        //catch all changed starred addresses

        if (output != null && output != NO_CONNECTION) {

            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val address = token.getString(JSON_ADDRESS)
                val addressObject = AddressBook.getAddressObject(address)
                val amount = addressObject.amount
                val oldBalance = addressObject.amountOld
                val timestamp = addressObject.timestampChange

                Log.d("mybroadcastreceiver", "prococess finished " + output + " size is " + changedAddressObjects.size + " old balance " + oldBalance + " new balance " + (Date().time - timestamp))
                if (amount != oldBalance && Date().time - timestamp < 1000 * 10)
                    changedAddressObjects.add(addressObject)
            }
        }

    }

}
