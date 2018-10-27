package com.joegruff.viacoinaddressscanner.helpers

import android.location.Address
import android.os.Handler
import android.os.Message
import android.util.Log
import com.joegruff.viacoinaddressscanner.R
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*
import kotlin.concurrent.schedule

const val JSON_AMOUNT = "amount"
const val JSON_ADDRESS = "address"
const val JSON_TITLE = "title"
const val JSON_TIMESTAMP_CHANGE = "timestampchange"
const val JSON_TIMESTAMP_CHECK = "timestampcheck"
const val JSON_AMOUNT_OLD = "amountold"
const val JSON_BEING_WATCHED = "beingwatched"

class AddressObject() : AsyncObserver {


    var address = ""
    var title = ""
    var amount = -1.0
    var isUpdating = false
    var isValid = false
    var isBeingWatched = false
    var amountOld = 0.0
    var timestampChange = Date().time.toDouble()
    var timestampCheck = timestampChange
    //fist delegate is ui and second is for alarmmanager
    var delegates = mutableListOf<AsyncObserver?>(null, null)

    constructor(jsonObject: JSONObject) : this() {
        address = if (jsonObject.has(JSON_ADDRESS)) jsonObject.getString(JSON_ADDRESS) else address
        title = if (jsonObject.has(JSON_TITLE)) jsonObject.getString(JSON_TITLE) else title
        amount = if (jsonObject.has(JSON_AMOUNT)) jsonObject.getDouble(JSON_AMOUNT) else amount
        amountOld = if (jsonObject.has(JSON_AMOUNT_OLD)) jsonObject.getDouble(JSON_AMOUNT_OLD) else amountOld
        timestampChange = if (jsonObject.has(JSON_TIMESTAMP_CHANGE)) jsonObject.getDouble(JSON_TIMESTAMP_CHANGE) else timestampChange
        timestampCheck = if (jsonObject.has(JSON_TIMESTAMP_CHECK)) jsonObject.getDouble(JSON_TIMESTAMP_CHECK) else timestampCheck
        isBeingWatched = if (jsonObject.has(JSON_BEING_WATCHED)) jsonObject.getBoolean(JSON_BEING_WATCHED) else isBeingWatched
        isValid = true
    }

    constructor(add: String) : this() {
        address = add
        update()
    }

    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(JSON_ADDRESS, address)
        jsonObject.put(JSON_TITLE, title)
        jsonObject.put(JSON_AMOUNT, amount)
        jsonObject.put(JSON_AMOUNT_OLD, amountOld)
        jsonObject.put(JSON_TIMESTAMP_CHANGE, timestampChange)
        jsonObject.put(JSON_TIMESTAMP_CHECK, timestampCheck)
        jsonObject.put(JSON_BEING_WATCHED, isBeingWatched)
        return jsonObject
    }


    override fun processbegan() {
        isUpdating = true
        try {
            //Log.d("addressobject", "processbegin and num delegates " + delegates?.size)

            delegates.forEach {
                if (it != null) {
                    it.processbegan()
                }
            }

        } catch (e: Exception) {
            //Log.d("addressobject", "processbegin " + e.printStackTrace())
        }

    }

    override fun balanceSwirlNotNull(): Boolean {
        return try {
            delegates[0]!!.balanceSwirlNotNull()
        } catch (e: java.lang.Exception) {
            false
        }
    }


    fun update(checkIfShown: Boolean = true) {

        if (checkIfShown)
            try {
                if (!delegates[0]!!.balanceSwirlNotNull()) {
                    return
                }
            } catch (e: java.lang.Exception) {

            }

        if (!isUpdating) {
            isUpdating = true
            GetInfoFromWeb(this, address).execute()
        }
    }

    fun updateIfFiveMinPast(){
        if (Date().time - timestampCheck > (1000 * 60 * 5))
            update(false)
    }

    override fun processfinished(output: String?) {

        var sendToDelegates = output


        if (output != null) {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val addressString = token.getString("addrStr")
                //Log.d("time", "elapsed hrs since change " + elapsedHrsSinceChange + " and oldest timestamp " + oldestTimestamp + " and current time " + Date().time.toDouble())
                if (address == addressString) {
                    val amountString = token.getString("balance")
                    val amountDoubleFromString = amountString.toDouble()
                    timestampCheck = Date().time.toDouble()
                    val elapsedHrsSinceChange = (timestampCheck - timestampChange) / (1000 * 60 * 60)
                    if (amount < 0) {
                        //dont imply a change if this is the initiation
                        timestampChange = timestampCheck
                        amountOld = amountDoubleFromString
                        amount = amountOld
                    } else if (amount != amountDoubleFromString) {
                        //record change
                        amountOld = amount
                        amount = amountDoubleFromString
                        timestampChange = timestampCheck
                    } else if (elapsedHrsSinceChange > 24) {
                        Log.d("24hrspassed", "forgot oldestamount because : " + elapsedHrsSinceChange + " hours have passed")
                        //forget older changes
                        timestampChange = timestampCheck
                        amountOld = amount
                    }

                    //Log.d("addressobject", "process finished " + output)
                    sendToDelegates = toJSON().toString()
                    if (!isValid) {
                        isValid = true
                    }
                }
            }
        }
        isUpdating = false

        try {
            delegates.forEach {
                if (it != null) {
                    it.processfinished(sendToDelegates)
                }
            }
        } catch (e: Exception) {

        }


    }

    fun IntRange.random() = Random().nextInt((endInclusive + 1) - start) + start

    /*fun <T> tryBlock (jsonObject: JSONObject, name: String, defaultValue : T) : T {
        return try {
            if (T is String) {
                jsonObject.getString(name)
            } else {
                defaultValue
            }
        }catch (e: JSONException) {
             defaultValue


    }*/

}