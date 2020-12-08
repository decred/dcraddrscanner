package com.joegruff.decredaddressscanner.helpers

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity

import java.text.DecimalFormat
import kotlin.math.pow

object AddressBook {
    val addresses = ArrayList<AddressObject>()
    private var gotAddressesAlready = false

    fun fillAddressBook(act: Activity?) {
        if (gotAddressesAlready) {
            return
        }
        if (act != null) {
            JSONSerializer.getAddresses(act.applicationContext)
                ?.let { addresses += it.asIterable() }
            gotAddressesAlready = true
        }
    }

    fun updateAddresses(force: Boolean = false) {
        addresses.forEach {
            if (force) it.update(false) else it.updateIfFiveMinPast()
        }
    }

    fun fillAddressBook(ctx: Context?) {
        if (gotAddressesAlready) {
            return
        }
        if (ctx != null) {
            JSONSerializer.getAddresses(ctx)?.let { addresses += it.asIterable() }
            gotAddressesAlready = true
        }
    }

    fun saveAddressBook(act: FragmentActivity?) {
        if (gotAddressesAlready)
            JSONSerializer.saveJSON(act?.applicationContext, addresses)
    }

    fun saveAddressBook(ctx: Context?) {
        if (gotAddressesAlready)
            JSONSerializer.saveJSON(ctx, addresses)
    }

    fun getAddressObject(address: String): AddressObject {
        for (a in addresses) {
            if (a.address == address)
                return a
        }
        return AddressObject(address)
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

    fun abbreviatedAmountFromString(amountString: String): String {
        var x = amountString.toDouble()
        var i = 0
        var subfix = ""
        if (x >= 10) {
            while (x >= 10) {
                x /= 10
                i += 1
            }
        } else if (x < 1 && x > 0) {
            while (x < 1) {
                x *= 10
                i -= 1
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
        x *= 10.0.pow(i.toDouble())
        val f = DecimalFormat("#.###")
        return f.format(x) + subfix
    }

}