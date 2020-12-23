package com.joegruff.decredaddressscanner.types

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect


const val mainNet = "https://explorer.dcrdata.org/api/"
const val testNet = "https://testnet.dcrdata.org/api/"

private var addrBook: AddressBook? = null

fun addrBook(ctx: Context) : AddressBook {
    if (addrBook != null) return addrBook as AddressBook
    val applicationScope = CoroutineScope(SupervisorJob())
    val db = MyDatabase.getDatabase(ctx, applicationScope)
    addrBook = AddressBook(db.addrDao(), ctx)
    return addrBook!!
}

class AddressBook(private val addrDao: AddressDao, private val ctx: Context) {
    val addresses = addresses()
    var url = mainNet

    fun setURL(url: String) {
        this.url = url
    }

    fun url(): String {
        return this.url
    }

    // TODO: It would probably be better to make everything asynchronous and use the db more
    //  cleverly, but for now just get the values once on startup and save as we go.
    private fun addresses(): ArrayList<Address> {
        val flowAddrs: Flow<List<Address>> = addrDao.getAll()
        val addrs = ArrayList<Address>()
        GlobalScope.launch {
            flowAddrs.collect {
                addrs.addAll(it)
                this.cancel()
            }
        }
        return addrs
    }

    fun updateBalances(force: Boolean = false) {
        GlobalScope.launch {
            addresses.forEach {
                if (force) it.update(ctx) else it.updateIfFiveMinPast(ctx)
                addrDao.update(it)
            }
        }
    }

    fun updateAddress(addr: Address, force: Boolean = false) {
        GlobalScope.launch {
            if (force) addr.update(ctx) else addr.updateIfFiveMinPast(ctx)
            addrDao.update(addr)
        }
    }

    fun insert(addr: Address, idx: Int = addresses.size) {
        addresses.add(idx, addr)
        GlobalScope.launch {
            addrDao.insert(addr)
        }
    }

    fun delete(addr: Address) {
        addresses.remove(addr)
        GlobalScope.launch {
            addrDao.delete(addr)
        }
    }

    fun getAddress(address: String): Address {
        var a: Address? = null
        addresses.forEach { addr: Address ->
            if (addr.address == address) a = addr
        }
        if (a != null) return a as Address
        a = newAddress(address, ctx)
        return a!!
    }
}