package com.joegruff.viacoinaddressscanner.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import com.joegruff.viacoinaddressscanner.MainActivity
import com.joegruff.viacoinaddressscanner.R
import com.joegruff.viacoinaddressscanner.ViewAddressFragment
import org.json.JSONObject
import org.json.JSONTokener
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import java.util.*


const val CHANNEL_ID = "com.joegruff.viacoinaddressscanner.notification_channel"
const val NOTIFICATION_ID = 1337

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver() {

    var changedaddresses = ArrayList<String>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null)
            createNotificationChannel(context)


        if (!AddressBook.gotAddressesAlready) {
            AddressBook.fillAddressBook(context)
        }else {
            Log.d("mybroadcastreceiver", "returned cause got addressesalready")
            return
        }


        //check for starred addresses
        for (starredAddress in AddressBook.addresses.filter { it.isBeingWatched }) {
            starredAddress.delegates[1] = this
            GetInfoFromWeb(starredAddress, starredAddress.address).execute()
            Log.d("mybroadcastreceiver", "onreceive fired " + starredAddress.address)
        }


        //give it five seconds to find changed addresses, report results as alert if something changed
        Handler().postDelayed({

            var message = changedaddresses.size.toString()
            var myPendingIntent :PendingIntent? = null

            if (changedaddresses.size < 1) {
                return@postDelayed
            } else if (changedaddresses.size < 2) {

                val token = JSONTokener(changedaddresses[0]).nextValue() as JSONObject

                var title = token.getString(JSON_TITLE)
                val address = token.getString(JSON_ADDRESS)
                if (title.equals(""))
                    title = address
                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_OLD_AMOUNT)

                val formattedAmountString = setAmounts(amountString, oldBalance)


                if (context != null) {
                    message = context.getString(R.string.changed_amounts_one, title, formattedAmountString)
                    val myNotificationIntent = Intent(context, ViewAddressActivity  ::class.java)
                    myNotificationIntent.putExtra(ViewAddressFragment.INTENT_DATA,address)
                    myNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    myPendingIntent = TaskStackBuilder.create(context).run {
                        addNextIntentWithParentStack(myNotificationIntent)
                        getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
                    }


                }

            } else {
                if (context != null) {
                    val myNotificationIntent = Intent(context, MainActivity::class.java)
                    myNotificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    myPendingIntent = PendingIntent.getActivity(context, 0, myNotificationIntent, 0)
                    message = context.getString(R.string.changed_amounts_many)

                    /*changedaddresses.forEach {
                        val token = JSONTokener(it).nextValue() as JSONObject
                        val address = token.getString(JSON_ADDRESS)
                        message = message + ":" + address.substring(0,7)
                    }*/
                }
            }

            if (context != null) {


                val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.small_coin_icon)
                        .setContentTitle(context.getString(R.string.changed_amounts_notification_title))
                        .setContentText(message)
                        .setContentIntent(myPendingIntent)
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
        } else {
            text = "0"
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
                val amount = token.getDouble(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_OLD_AMOUNT)
                val timestamp = token.getDouble(JSON_TIMESTAMP)
                Log.d("mybroadcastreceiver", "prococess finished " + output + " size is " + changedaddresses.size + " old balance " + oldBalance + " new balance " + amount)
                if (!amount.equals(oldBalance) && Date().time - timestamp < 1000 * 10)
                    changedaddresses.add(output)
            }

        }
    }
}