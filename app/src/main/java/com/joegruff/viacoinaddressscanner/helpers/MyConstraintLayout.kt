package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
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
        Log.d("mycontraintlayout","process began")
        this.clearAnimation()
        balance_swirl_progress_bar.alpha = .7f
        this.invalidate()
    }

    override fun processfinished(output: String?) {


        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {
                val amountString = token.getString(JSON_AMOUNT)
                balance_swirl_balance.setText(amountfromstring(amountString))
            }
        }
        balance_swirl_progress_bar.alpha = 0f

    }

    fun setAmount(s : String){
        balance_swirl_balance.text = amountfromstring(s)
    }

    fun amountfromstring(amountString:String) : String {
        if (abbreviatedValues){
            return AddressBook.abbreviatedAmountfromstring(amountString)
        }
        return amountString
    }
}