package com.joegruff.viacoinaddressscanner

import android.app.Activity
import android.arch.lifecycle.ViewModel

object AddressBook {
    var addresses: ArrayList<AddressObject>? = null
    var currentAddress: AddressObject? = null

    fun fillAddressBook(act: Activity) {
        addresses = JSONSerializer.getAddresses(act.applicationContext)
        addresses?.let { return }
        addresses = ArrayList()
    }

    fun saveAddressBook(act: Activity) {
        addresses?.let { JSONSerializer.saveJSON(act.applicationContext, it) }
    }

    fun addAddress(addressObject: AddressObject) {
        addresses?.add(addressObject)
    }

    fun removeAddress(address: String) {
        addresses?.let {
            for (a in it)
                if (a.address.equals(address)) {
                    it.remove(a)
                }
        }
    }
}