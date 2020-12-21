package com.joegruff.decredaddressscanner.types

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.joegruff.decredaddressscanner.R
import org.json.JSONObject
import org.json.JSONTokener
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
    var myAddress = ""

    override fun processBegan() {
        val handler = android.os.Handler(context.mainLooper)
        val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
        handler.post { swirl.visibility = View.VISIBLE }
        handler.postDelayed({ swirl.visibility = View.INVISIBLE }, 3000)
    }

    override fun processFinished(output: String) {
        val handler = android.os.Handler(context.mainLooper)
        val swirl = findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
        handler.post { swirl.visibility = View.INVISIBLE }
        if (output != "") {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_AMOUNT_OLD)
                val address = token.getString(JSON_ADDRESS)
                if (address != myAddress) {
                    Log.d("myConstraint ", "wrong address")
                    return
                }
                setAmounts(amountString, oldBalance)
            }
        }
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

    private fun amountFromString(amountString: String): String {
        if (abbreviatedValues) {
            return AddressBook.abbreviatedAmountFromString(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }

    private val swirlLayout = findViewById<MyConstraintLayout>(R.id.balance_swirl_layout)
    override fun balanceSwirlNotNull() = swirlLayout.isShown
}