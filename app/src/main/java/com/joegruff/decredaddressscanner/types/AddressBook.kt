package com.joegruff.decredaddressscanner.types

import android.content.Context
import kotlinx.coroutines.runBlocking


class AddressBook(private val addrDao: AddressDao, private val ctx: Context) {
    companion object {
        @Volatile
        private var addrBook: AddressBook? = null
        fun get(
            ctx: Context,
        ): AddressBook {
            return addrBook ?: synchronized(this) {
                val db = MyDatabase.get(ctx)
                val instance = AddressBook(db.addrDao(), ctx)
                addrBook = instance
                instance
            }
        }
    }

    // NOTE: Not volatile? Various threads may be accessing this. Not sure how concurrency is
    // handled here. Watch for problems.
    val addresses = addresses()

    // TODO: It would probably be better to make everything asynchronous and use the db more
    //  cleverly, but for now just get the values once on startup and save as we go.
    private fun addresses(): ArrayList<Address> {
        val addrs = ArrayList<Address>()
        runBlocking {
                addrs.addAll(addrDao.getAll())
        }
        return addrs
    }

    fun updateBalances(force: Boolean = false) {
        runBlocking {
            addresses.forEach {
                if (force) it.update(ctx) else it.updateIfFiveMinPast(ctx)
                addrDao.update(it)
            }
        }
    }

    fun updateAddress(addr: Address, force: Boolean = false) {
        runBlocking {
            if (force) addr.update(ctx) else addr.updateIfFiveMinPast(ctx)
            addrDao.update(addr)
        }
    }

    fun insert(addr: Address, idx: Int = addresses.size) {
        addresses.add(idx, addr)
        runBlocking {
            addrDao.insert(addr)
        }
    }

    fun delete(addr: Address) {
        addresses.remove(addr)
        runBlocking {
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