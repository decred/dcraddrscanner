package com.joegruff.decredaddressscanner.helpers

interface AsyncObserver {
    fun processfinished(output: String?)
    fun processbegan()
    fun balanceSwirlNotNull() = false
}