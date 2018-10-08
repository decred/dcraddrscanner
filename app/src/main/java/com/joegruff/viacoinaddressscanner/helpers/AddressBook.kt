package com.joegruff.viacoinaddressscanner.helpers

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import java.text.DecimalFormat

object AddressBook {
    val addresses = ArrayList<AddressObject>()
    var gotAddressesAlready = false

    fun fillAddressBook(act: Activity?) {
        if (gotAddressesAlready) {
            return
        }
        if (act != null) {
            JSONSerializer.getAddresses(act.applicationContext)?.let {addresses += it.asIterable()}
            gotAddressesAlready = true
        }

    }

    fun fillAddressBook(ctx: Context?, startUpdatingNow: Boolean = true) {
        if (gotAddressesAlready) {
            return
        }
        if (ctx != null) {
            JSONSerializer.getAddresses(ctx)?.let {addresses += it.asIterable()}
            gotAddressesAlready = true
        }

    }

    fun saveAddressBook(act: FragmentActivity?) {
        JSONSerializer.saveJSON(act?.applicationContext, addresses)
    }

    fun saveAddressBook(ctx: Context?) {
        JSONSerializer.saveJSON(ctx, addresses)
    }


    fun newObjectFromAddress(address: String): AddressObject? {
        for (a in addresses) {
            if (a.address == address)
                return a
        }
        val newObject = AddressObject(address)
        //addresses.add(newObject)
        return newObject
    }

    fun updateAddress(addressObject: AddressObject?) {
        if (addressObject == null) {
            return
        }
        for (a in addresses) {

            if (a.address == addressObject.address) {
                a.amount = addressObject.amount
                a.title = addressObject.title
                return
            }
        }
        if (addressObject.isValid)
            this.addresses.add(addressObject)
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

    fun abbreviatedAmountfromstring(amountString: String): String {
        var x = amountString.toDouble()
        var i = 0
        var subfix = ""
        if (x >= 10) {
            while (x >= 10) {
                x = x / 10
                i += 1
                //Log.d("this ix i", "this is i " + i + " and x " + x)
            }
        } else if (x < 1 && x > 0) {
            while (x < 1) {
                x = x * 10
                i -= 1
                //Log.d("this ix i", "this is i " + i + " and x " + x)
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
                i -= 6
            }
            in 9..11 -> {
                subfix = "B"
                i -= 9
            }
            in 12..14 -> {
                subfix = "T"
                i -= 12
            }
            in 15..17 -> {
                subfix = "P"
                i -= 15
            }
            else -> {
            }
        }
        x = x * Math.pow(10.0, i.toDouble())
        val f = DecimalFormat("#.###")
        return f.format(x) + subfix
    }

}