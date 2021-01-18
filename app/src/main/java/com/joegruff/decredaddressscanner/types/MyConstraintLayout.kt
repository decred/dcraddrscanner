package com.joegruff.decredaddressscanner.types

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.joegruff.decredaddressscanner.R
import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.coroutines.CoroutineContext

class MyConstraintLayout : RelativeLayout, AsyncObserver, CoroutineScope {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override val coroutineContext: CoroutineContext
        get() = MainScope().coroutineContext

    var abbreviatedValues = false

    override fun processBegan() {
        launch {
            synchronized(this) {
                val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
                swirl?.visibility = View.VISIBLE
            }
        }
    }

    override fun processFinished(addr: Address, ctx: Context) {
        setUI(addr)
    }

    fun setUI(addr: Address) {
        launch {
            synchronized(this) {
                val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
                swirl?.visibility = View.INVISIBLE
                setAmounts(addr.amount.toString(), addr.amountOld.toString())
                setTicketStatus(addr.ticketStatus)
            }
        }
    }

    override fun processError(str: String) {
        launch {
            synchronized(this) {
                val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
                swirl?.visibility = View.INVISIBLE
            }
        }
    }

    fun setAmounts(balance: String, oldBalance: String) {
        val changeView = findViewById<TextView>(R.id.balance_swirl_change)
        val balanceView = findViewById<TextView>(R.id.balance_swirl_balance)
        val difference = balance.toDouble() - oldBalance.toDouble()
        val differenceText =
            when {
                difference > 0.0 -> {
                    changeView?.setTextColor(
                        ActivityCompat.getColor(
                            this.context,
                            R.color.Green
                        )
                    )
                    "+" + amountFromString(difference.toString())
                }
                difference < 0.0 -> {
                    changeView?.setTextColor(ActivityCompat.getColor(this.context, R.color.Red))
                    amountFromString(difference.toString())
                }
                else -> ""
            }
        balanceView?.text = amountFromString(balance)
        changeView?.text = differenceText
    }

    fun setTicketStatus(statusStr: String) {
        val colorInt = when (ticketStatusFromName(statusStr)) {
            TicketStatus.UNMINED, TicketStatus.IMMATURE, TicketStatus.LIVE -> R.color.Blue
            TicketStatus.VOTED, TicketStatus.SPENDABLE, TicketStatus.SPENT -> R.color.Green
            else -> R.color.Red
        }
        val statusView = findViewById<TextView>(R.id.balance_swirl_ticket_status)
        statusView?.setTextColor(ActivityCompat.getColor(this.context, colorInt))
        statusView?.text = statusStr
    }

    private fun amountFromString(amountString: String): String {
        if (abbreviatedValues) {
            return abbreviatedAmountFromString(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }

    override fun balanceSwirlIsShown(): Boolean {
        synchronized(this) {
            val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
            return swirl?.isShown ?: false
        }
    }
}