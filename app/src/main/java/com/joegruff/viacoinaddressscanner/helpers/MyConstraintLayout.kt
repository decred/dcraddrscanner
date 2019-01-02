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
    var myAddress = ""

    override fun processbegan() {
       // Log.d("mycontraintlayout","process began")
        //this.clearAnimation()
        val handler = android.os.Handler(context.mainLooper)
        handler.post({ balance_swirl_progress_bar.visibility = View.VISIBLE })
        handler.postDelayed({goInvis()}, 3000)
        //handler.postDelayed({balance_swirl_progress_bar.visibility = View.INVISIBLE},5000)

        //this.invalidate()
    }

    override fun processfinished(output: String?) {
        goInvis()

        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {

                val amountString = token.getString(JSON_AMOUNT)
                val oldBalance = token.getString(JSON_AMOUNT_OLD)
                val address = token.getString(JSON_ADDRESS)
                if (address != myAddress) {
                    Log.d("myConstraint ","wrong address")
                    return
                }
                setAmounts(amountString, oldBalance)
            }
        }


    }

    fun goInvis(){
        //val handler = android.os.Handler(context.mainLooper)
        //handler.post({balance_swirl_progress_bar.visibility = View.INVISIBLE})
        balance_swirl_progress_bar.visibility = View.INVISIBLE
        Log.d("myconstraint ","went invis")
    }

    //override fun onVisibilityChanged(changedView: View?, visibility: Int) {

        //if (visibility == View.INVISIBLE)
         //   goInvis()

     //   super.onVisibilityChanged(changedView, visibility)
    //}

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