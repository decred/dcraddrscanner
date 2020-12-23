package com.joegruff.decredaddressscanner.types

import android.content.Context

interface AsyncObserver {
    fun processFinished(addr: Address, ctx: Context)
    fun processBegan()
    fun processError(str: String)
    fun balanceSwirlIsShown() = true
}