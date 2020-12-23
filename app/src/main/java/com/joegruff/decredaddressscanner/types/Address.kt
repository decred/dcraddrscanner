package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject
import org.json.JSONTokener
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow

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
    // Amounts in coins.
    @ColumnInfo(name = AMOUNT) var amount: Double = 0.0,
    @ColumnInfo(name = AMOUNT_OLD) var amountOld: Double = 0.0,
    @ColumnInfo(name = TITLE) var title: String = "",
    @ColumnInfo(name = TIMESTAMP_CHANGE) var timestampChange: Double = Date().time.toDouble(),
    @ColumnInfo(name = TIMESTAMP_CHECK) var timestampCheck: Double = timestampChange,
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

    override fun balanceSwirlIsShown(): Boolean {
        return delegates[0]?.balanceSwirlIsShown() ?: false
    }

    override fun processError(str: String) {}

    fun update(ctx: Context) {
        if (!isUpdating) {
            isUpdating = true
            GetInfoFromWeb(this, address, ctx).execute()
        }
    }

    fun updateIfFiveMinPast(ctx: Context) {
        if (Date().time - timestampCheck > (1000 * 60 * 5))
            update(ctx)
    }

    override fun processFinished(addr: Address, ctx: Context) {
        if (address != addr.address) {
            throw Exception("updating wrong addr")
        }
        timestampCheck = addr.timestampCheck
        val elapsedHrsSinceChange =
            (timestampCheck - timestampChange) / (1000 * 60 * 60)
        when {
            !isValid -> {
                // New address copy retrieved values.
                amount = addr.amount
                amountOld = addr.amountOld
                timestampChange = addr.timestampChange
                timestampCheck = addr.timestampCheck
                // Because we were able to fetch it, it must be valid.
                isValid = true
                AddressBook.get(ctx).insert(this)
            }
            amount != addr.amount -> {
                // Record change.
                amountOld = amount
                amount = addr.amount
                timestampChange = timestampCheck
                AddressBook.get(ctx).updateAddress(this)
            }
            elapsedHrsSinceChange > 24 -> {
                // Forget older changes.
                timestampChange = timestampCheck
                amountOld = amount
                AddressBook.get(ctx).updateAddress(this)
            }
        }
        isUpdating = false
        delegates.forEach {
            it?.processFinished(this, ctx)
        }
    }
}

fun newAddress(add: String, ctx: Context): Address {
    val a = Address(add)
    a.update(ctx)
    return a
}

fun addrFromWebJSON(str: String): Address {
    val token = JSONTokener(str).nextValue()
    if (token is JSONObject) {
        val a = Address(token.getString(ADDRESS))
        val amountString = token.getString(AMOUNT)
        val amountDoubleFromString = amountString.toDouble()
        a.amount = amountDoubleFromString
        a.amountOld = amountDoubleFromString
        return a
    }
    throw Exception("unknown JSON")
}

fun abbreviatedAmountFromString(amountString: String): String {
    var x = amountString.toDouble()
    var i = 0
    var suffix = ""
    if (x >= 10) {
        while (x >= 10) {
            x /= 10
            i += 1
        }
    } else if (x < 1 && x > 0) {
        while (x < 1) {
            x *= 10
            i -= 1
        }
    }

    when (i) {
        in -12..-10 -> {
            suffix = "p"
            i -= -12
        }
        in -9..-7 -> {
            suffix = "n"
            i -= -9
        }
        in -6..-4 -> {
            suffix = "μ"
            i -= -6
        }
        in 3..5 -> {
            suffix = "k"
            i -= 3
        }
        in 6..8 -> {
            suffix = "M"
            i -= 6
        }
        in 9..11 -> {
            suffix = "B"
            i -= 9
        }
        in 12..14 -> {
            suffix = "T"
            i -= 12
        }
        in 15..17 -> {
            suffix = "P"
            i -= 15
        }
        else -> {
        }
    }
    x *= 10.0.pow(i.toDouble())
    val f = DecimalFormat("#.##")
    return f.format(x) + suffix
}