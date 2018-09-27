package com.joegruff.viacoinaddressscanner.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.provider.Settings.Global.getString
import android.support.v4.app.NotificationCompat
import com.joegruff.viacoinaddressscanner.MainActivity
import com.joegruff.viacoinaddressscanner.R
import com.joegruff.viacoinaddressscanner.ViewAddressFragment
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import org.json.JSONObject
import org.json.JSONTokener
import android.app.PendingIntent
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import java.util.*


const val CHANNEL_ID = "com.joegruff.viacoinaddressscanner.notification_channel"
const val NOTIFICATION_ID = 1337

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver() {

    var changedaddresses = ArrayList<String>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null)
            createNotificationChannel(context)

        AddressBook.fillAddressBook(context)


        //check for starred addresses
        for (starredAddress in AddressBook.addresses.filter { it.isBeingWatched }) {
            starredAddress.delegates[1] = this
            GetInfoFromWeb(starredAddress, starredAddress.address).execute()
            Log.d("mybroadcastreceiver", "onreceive fired " + starredAddress.address)
        }


        //give it five seconds to find changed addresses, report results as alert if something changed
        Handler().postDelayed({

            var message = ""
            var formattedAmountString = ""
            var intent: Intent = Intent()

            if (changedaddresses.size < 1) {
                return@postDelayed
            } else if (changedaddresses.size < 2) {

                val token = JSONTokener(changedaddresses[0]).nextValue() as JSONObject
                var address = ""
                var amountString = ""
                var oldBalance = ""

                address = token.getString(JSON_ADDRESS)
                amountString = token.getString(JSON_AMOUNT)
                oldBalance = token.getString(JSON_OLD_AMOUNT)
                formattedAmountString = setAmounts(amountString, oldBalance)


                if (context != null) {
                    message = context.getString(R.string.changed_amounts_one, address, formattedAmountString)
                    intent = Intent(context, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                }

            } else {
                if (context != null) {
                    message = context.getString(R.string.changed_amounts_many)
                    intent = Intent(context, MainActivity::class.java)
                }
            }

            if (context != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.small_coin_icon)
                        .setContentTitle(context.getString(R.string.changed_amounts_notification_title))
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(NOTIFICATION_ID, mBuilder.build())


            }

        }, 5000)


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
        var text = ""
        if (difference > 0.0) {
            //balance_swirl_change.setTextColor(resources.getColor(R.color.Green))
            text = "+" + amountfromstring(difference.toString())
        } else if (difference < 0.0) {
            //balance_swirl_change.setTextColor(resources.getColor(R.color.Red))
            text = "-" + amountfromstring(difference.toString())
        }
        return text
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
                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_OLD_AMOUNT)
                val timestamp = token.getDouble(JSON_TIMESTAMP)

                if (!amountString.equals(oldBalance) && Date().time - timestamp < 1000 * 10)
                    changedaddresses.add(output)
            }

        }
    }
}