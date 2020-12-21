package com.joegruff.decredaddressscanner.types

import org.json.JSONObject
import org.json.JSONTokener
import java.util.*

const val JSON_AMOUNT = "dcr_unspent"
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
    private var isUpdating = false
    var isValid = false
    var isBeingWatched = false
    var amountOld = 0.0
    var timestampChange = Date().time.toDouble()
    private var timestampCheck = timestampChange

    // Fist delegate is ui and second is for alarmManager
    var delegates = mutableListOf<AsyncObserver?>(null, null)

    constructor(jsonObject: JSONObject) : this() {
        address = if (jsonObject.has(JSON_ADDRESS)) jsonObject.getString(JSON_ADDRESS) else address
        title = if (jsonObject.has(JSON_TITLE)) jsonObject.getString(JSON_TITLE) else title
        amount = if (jsonObject.has(JSON_AMOUNT)) jsonObject.getDouble(JSON_AMOUNT) else amount
        amountOld =
            if (jsonObject.has(JSON_AMOUNT_OLD)) jsonObject.getDouble(JSON_AMOUNT_OLD) else amountOld
        timestampChange =
            if (jsonObject.has(JSON_TIMESTAMP_CHANGE)) jsonObject.getDouble(JSON_TIMESTAMP_CHANGE) else timestampChange
        timestampCheck =
            if (jsonObject.has(JSON_TIMESTAMP_CHECK)) jsonObject.getDouble(JSON_TIMESTAMP_CHECK) else timestampCheck
        isBeingWatched =
            if (jsonObject.has(JSON_BEING_WATCHED)) jsonObject.getBoolean(JSON_BEING_WATCHED) else isBeingWatched
        isValid = true
        updateIfFiveMinPast()
    }

    constructor(add: String) : this() {
        address = add
        update(checkIfShown = false, newAddress = true)
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


    override fun processBegan() {
        isUpdating = true
        try {
            delegates.forEach {
                it?.processBegan()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun balanceSwirlNotNull(): Boolean {
        return try {
            delegates[0]!!.balanceSwirlNotNull()
        } catch (e: java.lang.Exception) {
            false
        }
    }


    fun update(checkIfShown: Boolean = true, newAddress: Boolean = false) {
        if (checkIfShown)
            try {
                if (!delegates[0]!!.balanceSwirlNotNull()) {
                    return
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        if (!isUpdating) {
            isUpdating = true
            GetInfoFromWeb(this, address, newAddress).execute()
        }
    }

    fun updateIfFiveMinPast() {
        if (Date().time - timestampCheck > (1000 * 60 * 5))
            update(false)
    }

    override fun processFinished(output: String) {
        var sendToDelegates = output
        if (output != "") {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val addressString = token.getString(JSON_ADDRESS)
                if (address == addressString) {
                    val amountString = token.getString(JSON_AMOUNT)
                    val amountDoubleFromString = amountString.toDouble()
                    timestampCheck = Date().time.toDouble()
                    val elapsedHrsSinceChange =
                        (timestampCheck - timestampChange) / (1000 * 60 * 60)
                    when {
                        amount < 0 -> {
                            // Don`t imply a change if this is the initiation.
                            timestampChange = timestampCheck
                            amountOld = amountDoubleFromString
                            amount = amountOld
                        }
                        amount != amountDoubleFromString -> {
                            // Record change.
                            amountOld = amount
                            amount = amountDoubleFromString
                            timestampChange = timestampCheck
                        }
                        elapsedHrsSinceChange > 24 -> {
                            // Forget older changes.
                            timestampChange = timestampCheck
                            amountOld = amount
                        }
                    }
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
                it?.processFinished(sendToDelegates)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}