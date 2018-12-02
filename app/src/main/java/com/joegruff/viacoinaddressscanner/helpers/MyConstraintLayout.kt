package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.joegruff.viacoinaddressscanner.R
import com.joegruff.viacoinaddressscanner.R.id.balance_swirl_layout
import kotlinx.android.synthetic.main.balance_swirl.view.*
import org.json.JSONObject
import org.json.JSONTokener
import java.text.DecimalFormat
import java.util.logging.Handler

class MyConstraintLayout : RelativeLayout, AsyncObserver {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,attrs,defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    constructor(context: Context) : super(context)

    var abbreviatedValues = false

    override fun processbegan() {
       // Log.d("mycontraintlayout","process began")
        //this.clearAnimation()
        val handler = android.os.Handler(context.mainLooper)
        handler.post({balance_swirl_progress_bar.visibility = View.VISIBLE})
        //handler.postDelayed({balance_swirl_progress_bar.visibility = View.INVISIBLE},5000)

        //this.invalidate()
    }

    override fun processfinished(output: String?) {


        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {
                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_AMOUNT_OLD)
                setAmounts(amountString, oldBalance)
            }
        }
        goInvis()

    }

    fun goInvis(){
        balance_swirl_progress_bar.visibility = View.INVISIBLE
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {

        if (visibility == View.INVISIBLE)
            goInvis()
        //balance_swirl_progress_bar.alpha = 0f

        super.onVisibilityChanged(changedView, visibility)
    }

    fun setAmounts(balance : String, oldBalance : String){
        val difference = balance.toDouble() - oldBalance.toDouble()
        val differenceText =
        when {
            difference > 0.0 -> {
                balance_swirl_change.setTextColor(resources.getColor(R.color.Green))
                "+" + amountfromstring(difference.toString())
            }
            difference < 0.0 -> {
                balance_swirl_change.setTextColor(resources.getColor(R.color.Red))
                amountfromstring(difference.toString())
            }
            else -> ""
        }
        balance_swirl_balance.text = amountfromstring(balance)
        balance_swirl_change.text = differenceText
    }

    fun amountfromstring(amountString:String) : String {
        if (abbreviatedValues){
            return AddressBook.abbreviatedAmountfromstring(amountString)
        }
        val f = DecimalFormat("#.########")
        return f.format(amountString.toDouble()).toString()
    }

    override fun balanceSwirlNotNull() = balance_swirl_layout.isShown
}