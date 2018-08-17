package com.joegruff.viacoinaddressscanner.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class MyBroadcastReceiver : AsyncObserver, BroadcastReceiver(){

    var changedaddresses = ArrayList<String>()

    override fun onReceive(context: Context?, intent: Intent?) {
        AddressBook.fillAddressBook(null, context)


        for (starredAddress in AddressBook.addresses.filter {it.isBeingWatched}) {
            starredAddress.delegates?.set(1, this)
        GetInfoFromWeb(starredAddress ,starredAddress.address)
        }



    }

    override fun processbegan() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processfinished(output: String?) {
        if (output != null && output != NO_CONNECTION) {
            val token = JSONTokener(output).nextValue()



        }


        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}