package com.joegruff.viacoinaddressscanner.helpers

interface AsyncObserver {
    fun processfinished(output: String?)
    fun processbegan()
}