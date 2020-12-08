package com.joegruff.decredaddressscanner.helpers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.joegruff.decredaddressscanner.R
import kotlinx.android.synthetic.main.balance_swirl.view.*
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
        handler.post { balance_swirl_progress_bar.visibility = View.VISIBLE }
        handler.postDelayed({ goInvis() }, 3000)
    }

    override fun processFinished(output: String?) {
        goInvis()
        if (output != null) {
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

    private fun goInvis() {
        balance_swirl_progress_bar.visibility = View.INVISIBLE
    }

    fun setAmounts(balance: String, oldBalance: String) {
        val difference = balance.toDouble() - oldBalance.toDouble()
        val differenceText =
            when {
                difference > 0.0 -> {
                    balance_swirl_change.setTextColor(resources.getColor(R.color.Green))
                    "+" + amountFromString(difference.toString())
                }
                difference < 0.0 -> {
                    balance_swirl_change.setTextColor(resources.getColor(R.color.Red))
                    amountFromString(difference.toString())
                }
                else -> ""
            }
        balance_swirl_balance.text = amountFromString(balance)
        balance_swirl_change.text = differenceText
    }

    private fun amountFromString(amountString: String): String {
        if (abbreviatedValues) {
            return AddressBook.abbreviatedAmountFromString(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }

    override fun balanceSwirlNotNull() = balance_swirl_layout.isShown
}