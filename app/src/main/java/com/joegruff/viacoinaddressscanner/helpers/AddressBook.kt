package com.joegruff.viacoinaddressscanner.helpers

import android.app.Activity
import android.support.v4.app.FragmentActivity

object AddressBook {
    val addresses = ArrayList<AddressObject>()
    var gotAddressesAlready = false

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

    fun newObjectFromAddress(address : String): AddressObject? {
        for (a in addresses) {
            if (a.address == address)
                return a
        }
        val newObject = AddressObject(address)
        //addresses.add(newObject)
        return newObject
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
            if (a.address == address) {
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