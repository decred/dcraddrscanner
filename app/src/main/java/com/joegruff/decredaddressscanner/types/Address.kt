package com.joegruff.decredaddressscanner.types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.joegruff.decredaddressscanner.activities.AddrBook
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*

const val AMOUNT = "dcr_unspent"
const val ADDRESS_TABLE = "address_table"
const val ADDRESS = "address"
const val TITLE = "title"
const val TIMESTAMP_CHANGE = "timestamp_change"
const val TIMESTAMP_CHECK = "timestamp_check"
const val AMOUNT_OLD = "amount_old"
const val BEING_WATCHED = "being_watched"
const val VALID = "valid"

@Entity(tableName = ADDRESS_TABLE)
data class Address(
    @PrimaryKey val address: String,
    @ColumnInfo(name = AMOUNT) var amount: Double = -1.0,
    @ColumnInfo(name = TITLE) var title: String = "",
    @ColumnInfo(name = TIMESTAMP_CHANGE) var timestampChange: Double = Date().time.toDouble(),
    @ColumnInfo(name = TIMESTAMP_CHECK) var timestampCheck: Double = timestampChange,
    @ColumnInfo(name = AMOUNT_OLD) var amountOld: Double = 0.0,
    @ColumnInfo(name = BEING_WATCHED) var isBeingWatched: Boolean = false,
    @ColumnInfo(name = VALID) var isValid: Boolean = false,
) : AsyncObserver {
    @Ignore
    private var isUpdating = false

    // Fist delegate is ui and second is for alarmManager
    @Ignore
    var delegates = mutableListOf<AsyncObserver?>(null, null)


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
            GetInfoFromWeb(this, AddrBook.url(), address, newAddress).execute()
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
                val addressString = token.getString(ADDRESS)
                if (address == addressString) {
                    val amountString = token.getString(AMOUNT)
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
                    val addr = this
                    sendToDelegates = this.toString()
                    if (!isValid) {
                        isValid = true

                        GlobalScope.async { AddrBook.insert(addr) }
                    } else {
                        AddrBook.updateAddress(addr)
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

fun newAddress(add: String): Address {
    val a = Address(add)
    a.update(checkIfShown = false, newAddress = true)
    return a
}