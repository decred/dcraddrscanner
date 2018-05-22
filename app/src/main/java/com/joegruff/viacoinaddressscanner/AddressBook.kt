package com.joegruff.viacoinaddressscanner

import android.content.Context

object AddressBook {
    var addresses:ArrayList<AddressObject>? = null

    fun fillAddressBook (ctx:Context):Boolean{
        addresses=JSONSerializer.getAddresses(ctx)
        addresses?.let { return true }
        return false
    }

    fun saveAddressBook(ctx: Context){
        val tempaddresses = addresses
        tempaddresses?.let {JSONSerializer.saveJSON(ctx, tempaddresses)}
    }
}