package com.joegruff.decredaddressscanner.types

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.joegruff.decredaddressscanner.R
import java.text.DecimalFormat

class MyConstraintLayout : RelativeLayout, AsyncObserver {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    var abbreviatedValues = false

    @Volatile
    var processing = false

    override fun processBegan() {
        synchronized(processing) {
            if (processing) return
            processing = true
        }
        val handler = android.os.Handler(context.mainLooper)
        val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
        handler.post { swirl.visibility = View.VISIBLE }
        // If for some reason processFinished/Error is not called. Should not happen however.
        handler.postDelayed({
            synchronized(processing) {
                swirl.visibility = View.INVISIBLE
                processing = false
            }
        }, 10000)
    }

    override fun processFinished(addr: Address, ctx: Context) {
        synchronized(processing) {
            if (!processing) return
            processing = false
        }
        setUI(addr)
    }

    fun setUI(addr: Address) {
        val handler = android.os.Handler(context.mainLooper)
        val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
        handler.post { swirl.visibility = View.INVISIBLE }
        setAmounts(addr.amount.toString(), addr.amountOld.toString())
        setTicketStatus(addr.ticketStatus)
    }

    override fun processError(str: String) {
        synchronized(processing) {
            if (!processing) return
            processing = false
        }
        val handler = android.os.Handler(context.mainLooper)
        val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
        handler.post { swirl.visibility = View.INVISIBLE }
    }

    fun setAmounts(balance: String, oldBalance: String) {
        val difference = balance.toDouble() - oldBalance.toDouble()
        val changeView = findViewById<TextView>(R.id.balance_swirl_change)
        val balanceView = findViewById<TextView>(R.id.balance_swirl_balance)
        val differenceText =
            when {
                difference > 0.0 -> {
                    changeView.setTextColor(ActivityCompat.getColor(this.context, R.color.Green))
                    "+" + amountFromString(difference.toString())
                }
                difference < 0.0 -> {
                    changeView.setTextColor(ActivityCompat.getColor(this.context, R.color.Red))
                    amountFromString(difference.toString())
                }
                else -> ""
            }
        balanceView.text = amountFromString(balance)
        changeView.text = differenceText
    }

    fun setTicketStatus(statusStr: String) {
        val statusView = findViewById<TextView>(R.id.balance_swirl_ticket_status)
        val colorInt = when (ticketStatusFromName(statusStr)) {
            TicketStatus.UNMINED, TicketStatus.IMMATURE, TicketStatus.LIVE -> R.color.Blue
            TicketStatus.VOTED, TicketStatus.SPENDABLE, TicketStatus.SPENT -> R.color.Green
            else -> R.color.Red
        }
        statusView.setTextColor(ActivityCompat.getColor(this.context, colorInt))
        statusView.text = statusStr
    }

    private fun amountFromString(amountString: String): String {
        if (abbreviatedValues) {
            return abbreviatedAmountFromString(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }

    override fun balanceSwirlIsShown(): Boolean {
        val swirlLayout = findViewById<MyConstraintLayout>(R.id.balance_swirl_layout)
        return swirlLayout.isShown
    }
}