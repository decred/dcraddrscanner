package com.joegruff.decredaddressscanner.types

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch

import java.text.DecimalFormat
import kotlin.math.pow

const val mainNet = "https://explorer.dcrdata.org/api/"
const val testNet = "https://testnet.dcrdata.org/api/"

class AddressBook(private val addrDao: AddressDao) {
    val addresses = addresses()
    var url = mainNet

    fun setURL(url: String) {
        this.url = url
    }

    fun url(): String {
        return this.url
    }

    private fun addresses(): ArrayList<Address> {
        val flowAddrs: Flow<List<Address>> = addrDao.getAll()
        val addrs = ArrayList<Address>()
        Log.d("loggering", "updating addresses")
        GlobalScope.launch {
            flowAddrs.collect {
                addrs.addAll(it)
                this.cancel()
            }
        }
        return addrs
    }

    fun updateAddresses(force: Boolean = false) {
        GlobalScope.launch {
            addresses.forEach {
                if (force) it.update(false) else it.updateIfFiveMinPast()
                addrDao.update(it)
            }
        }
    }

    fun updateAddress(addr: Address, force: Boolean = false) {
        GlobalScope.launch {
            if (force) addr.update(false) else addr.updateIfFiveMinPast()
            addrDao.update(addr)
            Log.d("loggering", "updating address and isbeingwatched " + addr.isBeingWatched)
        }
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(addr: Address) {
        addrDao.insert(addr)
    }

    fun getAddress(address: String): Address {
        var a: Address? = null
        addresses.forEach { addr: Address ->
            if (addr.address == address) a = addr
        }
        if (a != null) return a as Address
        a = newAddress(address)
        return a!!
    }
}

fun abbreviatedAmountFromString(amountString: String): String {
    var x = amountString.toDouble()
    var i = 0
    var suffix = ""
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
            suffix = "p"
            i -= -12
        }
        in -9..-7 -> {
            suffix = "n"
            i -= -9
        }
        in -6..-4 -> {
            suffix = "μ"
            i -= -6
        }
        in 3..5 -> {
            suffix = "k"
            i -= 3
        }
        in 6..8 -> {
            suffix = "M"
            i -= 6
        }
        in 9..11 -> {
            suffix = "B"
            i -= 9
        }
        in 12..14 -> {
            suffix = "T"
            i -= 12
        }
        in 15..17 -> {
            suffix = "P"
            i -= 15
        }
        else -> {
        }
    }
    x *= 10.0.pow(i.toDouble())
    val f = DecimalFormat("#.###")
    return f.format(x) + suffix
}