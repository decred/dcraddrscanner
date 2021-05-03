package com.joegruff.decredaddressscanner.types

import android.content.Context

// AsyncObserver is used as a callback interface when updating. A processBegin will always be
// followed up by either processError (error) or processFinish (success). If erred, err is the
// reason for error. balanceSwirlIsShown is an attempt to discern if the user is currently looking
// at an address so that we don't send a notification of change if they are already viewing.
interface AsyncObserver {
    fun processBegin()
    fun processFinish(addr: Address, ctx: Context)
    fun processError(err: String)
    fun balanceSwirlIsShown() = true
}