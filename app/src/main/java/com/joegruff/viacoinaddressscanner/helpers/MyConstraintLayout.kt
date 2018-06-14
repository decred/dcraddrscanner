package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
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
        balance_swirl_progress_bar.alpha = 0.7f
    }

    override fun processfinished(output: String?) {


        if (output != null) {
            val token = JSONTokener(output).nextValue()

            if (token is JSONObject) {
                val amountString = token.getString("balance")
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
            var x = amountString.toDouble()
            var i = 0
            var subfix = ""
            if (x >= 10){
                while (x >= 10) {
                    x = x / 10
                    i += 1
                    Log.d("this ix i" ,"this is i " + i + " and x " + x)
                }
            } else if (x < 1 && x > 0) {
                while (x < 1) {
                    x = x * 10
                    i -= 1
                    Log.d("this ix i" ,"this is i " + i + " and x " + x)
                }
            }

            when (i) {
                in -12..-10 -> {
                    subfix = "p"
                    i -= -12
                }
                in -9..-7 -> {
                    subfix = "n"
                    i -= -9
                }
                in -6..-4 -> {
                    subfix = "μ"
                    i -= -6
                }
                in 3..5 -> {
                    subfix = "k"
                    i -= 3
                }
                in 6..8 -> {
                    subfix = "M"
                    i = 6
                }
                in 9..11 -> {
                    subfix = "B"
                    i = 9
                }
                in 12..14 -> {
                    subfix = "T"
                    i = 12
                }
                in 15..17 -> {
                    subfix = "P"
                    i = 15
                }
                else -> {}
            }
            x = x * Math.pow(10.0,i.toDouble())
            val f = DecimalFormat("#.###")
            return f.format(x) + subfix
        }
        return amountString
    }
}