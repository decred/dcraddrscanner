package com.joegruff.viacoinaddressscanner.helpers

import android.app.Activity
import android.support.v4.app.FragmentActivity

object AddressBook {
    val addresses = ArrayList<AddressObject>()
    var gotAddressesAlready = false
    var currentAddress = ""

    fun fillAddressBook(act: Activity) {
        if (gotAddressesAlready)
            return
        else
            JSONSerializer.getAddresses(act.applicationContext)?.let {
                addresses += it.asIterable()
                gotAddressesAlready = true
            }
    }

    fun saveAddressBook(act: FragmentActivity?) {
        JSONSerializer.saveJSON(act?.applicationContext, addresses)
    }

    fun addAddressAndIsNewAddress(addressObject: AddressObject): Boolean {
        for (a in addresses) {
            if (a.address == addressObject.address)
                return false
        }
        addresses.add(addressObject)
        return true
    }

    fun updateAddress(addressObject: AddressObject){
        for (a in addresses) {
            if (a.address == addressObject.address) {
                a.amount = addressObject.amount
                a.title = addressObject.title
            }
        }
    }

    fun removeAddress(address: String) {

        for (a in addresses)
            if (a.address.equals(address)) {
                addresses.remove(a)
            }

    }

    fun getAddress(address: String): AddressObject? {
        for (a in addresses) {
            if (a.address == address)
                return a
        }
        return null
    }
}