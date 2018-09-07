package com.joegruff.viacoinaddressscanner.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Handler
import android.provider.Settings.Global.getString
import com.joegruff.viacoinaddressscanner.R
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.StringReader
import java.text.DecimalFormat

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver(){

    var changedaddresses = ArrayList<String>()

    override fun onReceive(context: Context?, intent: Intent?) {
        AddressBook.fillAddressBook(context)

        //check for starred addresses
        for (starredAddress in AddressBook.addresses.filter {it.isBeingWatched}) {
            starredAddress.delegates[1] = this
        GetInfoFromWeb(starredAddress ,starredAddress.address)
        }


        //give it five seconds to find changed addresses, report results as alert if something changed
        Handler().postDelayed({
            var message = ""
            var formattedAmountString = ""
            if (changedaddresses.size < 1) {
                return@postDelayed
            } else if (changedaddresses.size < 2) {

                val token = JSONTokener(changedaddresses[0]).nextValue()
                var address = ""
                var amountString = ""
                var oldBalance = ""

                if (token is JSONObject) {
                    address = token.getString(JSON_ADDRESS)
                    amountString = token.getString(JSON_AMOUNT)
                    oldBalance = token.getString(JSON_OLD_AMOUNT)
                    formattedAmountString = setAmounts(amountString, oldBalance)
                }

                if (context != null)
                    message = context.getString(R.string.changed_amounts_one, address, formattedAmountString)
            } else {
                if (context != null)
                message = context.getString(R.string.changed_amounts_many)
            }

        }, 5000)




    }

    fun setAmounts(balance : String, oldBalance : String) : String{
        val difference = balance.toDouble() - oldBalance.toDouble()
        var text = ""
        if (difference > 0.0) {
            balance_swirl_change.setTextColor(resources.getColor(R.color.Green))
            text = "+" + amountfromstring(difference.toString())
        } else if (difference < 0.0){
            balance_swirl_change.setTextColor(resources.getColor(R.color.Red))
            text = "-" + amountfromstring(difference.toString())
        }
        balance_swirl_balance.text = amountfromstring(balance)
        balance_swirl_change.text = text
    }

    fun amountfromstring(amountString:String) : String {
        return AddressBook.abbreviatedAmountfromstring(amountString)
    }

    override fun processbegan() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processfinished(output: String?) {
        //catch all changed starred addresses
        if (output != null && output != NO_CONNECTION) {
            val token = JSONTokener(output).nextValue()
            changedaddresses.add(token as String)
        }
    }
}