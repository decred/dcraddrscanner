package com.joegruff.decredaddressscanner.helpers

interface AsyncObserver {
    fun processFinished(output: String?)
    fun processBegan()
    fun balanceSwirlNotNull() = false
}