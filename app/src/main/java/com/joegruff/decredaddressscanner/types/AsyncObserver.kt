package com.joegruff.decredaddressscanner.types

interface AsyncObserver {
    fun processFinished(output: String)
    fun processBegan()
    fun balanceSwirlNotNull() = false
}