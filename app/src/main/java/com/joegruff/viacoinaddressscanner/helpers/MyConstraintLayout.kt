package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.balance_swirl.view.*
import org.json.JSONObject
import org.json.JSONTokener

class MyConstraintLayout : RelativeLayout, AsyncObserver {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,attrs,defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    constructor(context: Context) : super(context)

    var abbreviatedValues = false




    override fun processbegan() {
        balance_swirl_progress_bar.alpha = 0.7f
    }

    override fun processfinished(output: String?) {


        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {
                val amountString = token.getString("balance")

                balance_swirl_balance.setText(amountString)
                Log.d("constraintlayout ", "process finished" + output)
            }
        }
        balance_swirl_progress_bar.alpha = 0f

    }
    fun amountfromstring(amountString:String) : String {
        if (abbreviatedValues){
            var x = amountString.toFloat()
            var i = 0
            var subfix = ""
            if (x >= 10){
                while (x >= 10) {
                    x = x / 10
                    i += 1
                }
            } else if (x < 0) {
                while (x < 0) {
                    x = x * 10
                    i -= 1
                }
            }



        }
        return amountString
    }
}