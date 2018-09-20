package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.joegruff.viacoinaddressscanner.R
import kotlinx.android.synthetic.main.balance_swirl.view.*
import org.json.JSONObject
import org.json.JSONTokener
import java.text.DecimalFormat

class MyConstraintLayout : RelativeLayout, AsyncObserver {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,attrs,defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    constructor(context: Context) : super(context)

    var abbreviatedValues = false

    override fun processbegan() {
       // Log.d("mycontraintlayout","process began")
        this.clearAnimation()
        balance_swirl_progress_bar.alpha = .7f
        this.invalidate()
    }

    override fun processfinished(output: String?) {


        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {
                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_OLD_AMOUNT)
                setAmounts(amountString, oldBalance)
            }
        }
        balance_swirl_progress_bar.alpha = 0f

    }

    fun setAmounts(balance : String, oldBalance : String){
        val difference = balance.toDouble() - oldBalance.toDouble()
        var text = ""
        if (difference > 0.0) {
            balance_swirl_change.setTextColor(resources.getColor(R.color.Green))
            text = "+" + amountfromstring(difference.toString())
        } else if (difference < 0.0){
            balance_swirl_change.setTextColor(resources.getColor(R.color.Red))
            text = "-" + amountfromstring(difference.toString())
        }
        balance_swirl_balance.text = amountfromstring(balance)
        balance_swirl_change.text = text
    }

    fun amountfromstring(amountString:String) : String {
        if (abbreviatedValues){
            return AddressBook.abbreviatedAmountfromstring(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }
}