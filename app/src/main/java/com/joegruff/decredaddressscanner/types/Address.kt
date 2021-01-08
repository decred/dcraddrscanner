package com.joegruff.decredaddressscanner.types

import android.content.Context
import android.util.JsonToken
import android.util.Log
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
const val TIMESTAMP_CREATE = "timestamp_create"
const val AMOUNT_OLD = "amount_old"
const val IS_BEING_WATCHED = "being_watched"
const val TICKET_STATUS = "ticket_status"
const val TICKET_TXID = "ticket_txid"
const val TICKET_EXPIRY = "ticket_expiry"
const val TICKET_MATURITY = "ticket_maturity"
const val TICKET_SPENDABLE = "ticket_spendable"
const val NETWORK = "network"
const val IS_VALID = "valid"

enum class TicketStatus(
    val Name: String,
) {
    UNMINED("unmined"),
    IMMATURE("immature"),
    LIVE("live"),
    VOTED("voted"),
    SPENDABLE("spendable"),
    SPENT("spent"),
    EXPIRED("expired"),
    MISSED("missed"),
    REVOKED("revoked"),
    UNKNOWN("unknown");
}

fun ticketStatusFromName(name: String): TicketStatus {
    return when (name) {
        TicketStatus.UNMINED.Name -> TicketStatus.UNMINED
        TicketStatus.IMMATURE.Name -> TicketStatus.IMMATURE
        TicketStatus.LIVE.Name -> TicketStatus.LIVE
        TicketStatus.VOTED.Name -> TicketStatus.VOTED
        TicketStatus.EXPIRED.Name -> TicketStatus.EXPIRED
        TicketStatus.SPENDABLE.Name -> TicketStatus.SPENDABLE
        TicketStatus.SPENT.Name -> TicketStatus.SPENT
        TicketStatus.REVOKED.Name -> TicketStatus.REVOKED
        TicketStatus.MISSED.Name -> TicketStatus.MISSED
        else -> TicketStatus.UNKNOWN
    }
}


// NOTE: Not sure if we need to worry about concurrency with separate fields. Only isUpdating is
// specifically locked. Watch for problems.
@Entity(tableName = ADDRESS_TABLE)
data class Address(
    @PrimaryKey var address: String,
    // Amounts in coins.
    @ColumnInfo(name = AMOUNT) var amount: Double = 0.0,
    @ColumnInfo(name = AMOUNT_OLD) var amountOld: Double = 0.0,
    @ColumnInfo(name = TITLE) var title: String = "",
    @ColumnInfo(name = TIMESTAMP_CHANGE) var timestampChange: Double = Date().time.toDouble(),
    @ColumnInfo(name = TIMESTAMP_CHECK) var timestampCheck: Double = timestampChange,
    @ColumnInfo(name = TIMESTAMP_CREATE) var timestampCreate: Double = timestampChange,
    @ColumnInfo(name = TICKET_EXPIRY) var ticketExpiry: Double = 0.0,
    @ColumnInfo(name = TICKET_MATURITY) var ticketMaturity: Double = 0.0,
    @ColumnInfo(name = TICKET_SPENDABLE) var ticketSpendable: Double = 0.0,
    @ColumnInfo(name = IS_BEING_WATCHED) var isBeingWatched: Boolean = false,
    @ColumnInfo(name = IS_VALID) var isValid: Boolean = false,
    @ColumnInfo(name = TICKET_STATUS) var ticketStatus: String = "",
    // The presence of ticketTXID indicates that this address is part of a commitment script.
    @ColumnInfo(name = TICKET_TXID) var ticketTXID: String = "",
    @ColumnInfo(name = NETWORK) var network: String = "",
) {
    @Ignore
    @Volatile
    private var isUpdating = false

    class DelegateListeners(){
        var swirl: AsyncObserver? = null
        var addrFragment: AsyncObserver? = null
        // other is either the broadcast receiver or a batch request.
        var other: AsyncObserver? = null
    }
    @Ignore
    val delegates = DelegateListeners()

    fun processBegan() {
        delegates.swirl?.processBegan()
        delegates.other?.processBegan()
        delegates.addrFragment?.processBegan()
    }

    fun balanceSwirlIsShown(): Boolean {
        return delegates.swirl?.balanceSwirlIsShown() ?: false
    }

    fun processError(str: String) {
        synchronized(isUpdating) {
            isUpdating = false
        }
        delegates.swirl?.processError(str)
        delegates.other?.processError(str)
        delegates.addrFragment?.processError(str)
    }

    fun update(ctx: Context) {
        synchronized(isUpdating) {
            if (isUpdating) return
            isUpdating = true
        }
        GetInfoFromWeb(this, ctx).execute()
    }

    fun updateIfFiveMinPast(ctx: Context) {
        if (Date().time - timestampCheck > (1000 * 60 * 5))
            update(ctx)
    }


    fun updateBalanceFromWebJSON(ctx: Context, str: String) {
        val token = JSONTokener(str).nextValue()
        if (token !is JSONObject) {
            throw Exception("unknown JSON")
        }
        if (token.getString(ADDRESS) != this.address) throw Exception("updating wrong address")
        this.updateBalance(ctx, token.getString(AMOUNT).toDouble())
        return
    }

    fun checkTicketLive(): Boolean {
        val now = Date().time.toDouble() / 1000
        if (now < this.ticketMaturity) {
            return false
        }
        this.ticketStatus = TicketStatus.LIVE.Name
        return true
    }

    fun checkTicketVotedWebJSON(str: String): Boolean {
        val token = JSONTokener(str).nextValue()
        if (token !is JSONObject) {
            throw Exception("unknown JSON")
        }
        val status = token.getString("status")
        if (status == TicketStatus.VOTED.Name) {
            this.ticketStatus = status
            return true
        }
        return false
    }

    fun checkTicketMissedWebJSON(str: String): Boolean {
        val token = JSONTokener(str).nextValue()
        if (token !is JSONObject) {
            throw Exception("unknown JSON")
        }
        val status = token.getString("status")
        if (status == TicketStatus.MISSED.Name) {
            this.ticketStatus = status
            return true
        }
        return false
    }

    fun checkTicketExpired(): Boolean {
        val now = Date().time.toDouble() / 1000
        if (now < this.ticketExpiry) {
            return false
        }
        this.ticketStatus = TicketStatus.EXPIRED.Name
        return true
    }

    fun checkTicketSpendable(): Boolean {
        val now = Date().time.toDouble() / 1000
        if (now < this.ticketSpendable) {
            return false
        }
        this.ticketStatus = TicketStatus.SPENDABLE.Name
        return true
    }


    fun checkTicketSpent(): Boolean {
        if (this.ticketStatus != TicketStatus.SPENDABLE.Name || this.amount != 0.0) {
            return false
        }
        this.ticketStatus = TicketStatus.SPENT.Name
        return true
    }

    fun initTicketFromWebJSON(token: JSONObject) {
        if (token.getString("txid") != this.ticketTXID) throw Exception("initializing wrong address")
        val outs = token.getJSONArray("vout")
        val len = outs.length()
        if (len != 3 && len != 5) throw Exception("wrong number of outputs for a ticket")
        val out = outs[len - 2] as JSONObject
        val scriptPubKey = out.getJSONObject("scriptPubKey")
        this.address = scriptPubKey.getJSONArray("addresses")[0] as String
        if (this.address == "") throw Exception("unable to obtain commitment address")
        this.network = netFromAddr(this.address).Name
        this.ticketStatus = TicketStatus.UNMINED.Name
    }

    fun checkTicketMinedWebJSON(token: JSONObject): Boolean {
        val block = token.optJSONObject("block") ?: return false
        Log.d("loggering", "block "+block)
        val minedTime = block.getInt("blocktime").toDouble()
        val net = netFromName(this.network)
        this.ticketMaturity = minedTime + (net.TicketMaturity * net.TargetTimePerBlock)
        this.ticketExpiry = minedTime + (net.TicketExpiry * net.TargetTimePerBlock)
        this.ticketStatus = TicketStatus.IMMATURE.Name
        return true
    }

    private fun updateBalance(ctx: Context, newAmount: Double) {
        timestampCheck = Date().time.toDouble()
        val elapsedHrsSinceChange =
            (timestampCheck - timestampChange) / (1000 * 60 * 60)
        when {
            !isValid -> {
                // New address copy retrieved values.
                amount = newAmount
                amountOld = newAmount
                timestampChange = timestampCheck
                // Because we were able to fetch it, it must be valid.
                isValid = true
                AddressBook.get(ctx).insert(this)
            }
            amount != newAmount -> {
                // Record change.
                amountOld = amount
                amount = newAmount
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
    }

    fun processFinished(ctx: Context) {
        synchronized(isUpdating) {
            isUpdating = false
        }
        delegates.swirl?.processFinished(this, ctx)
        delegates.other?.processFinished(this, ctx)
        delegates.addrFragment?.processFinished(this, ctx)
    }
}

fun newAddress(add: String, ticketTXID: String, delegate: AsyncObserver?, ctx: Context): Address {
    val a = Address(add)
    a.delegates.other = delegate
    a.ticketTXID = ticketTXID
    a.update(ctx)
    return a
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