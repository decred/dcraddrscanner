package com.joegruff.decredaddressscanner.types

import android.content.Context
import kotlinx.coroutines.runBlocking

private var addresses: ArrayList<Address>? = null

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

    // TODO: It would probably be better to make everything asynchronous and use the db more
    //  cleverly, but for now just get the values once on startup and save as we go.
    fun addresses(): ArrayList<Address> {
        return addresses ?: synchronized(this) {
            val addrs = ArrayList<Address>()
            runBlocking {
                addrs.addAll(addrDao.getAll())
            }
            addresses = addrs
            addrs
        }
    }

    // It is expected that addr is already an element in addresses.
    fun update(addr: Address) {
        runBlocking {
            addrDao.update(addr)
        }
    }

    fun insert(addr: Address, idx: Int = addresses().size) {
        addresses().add(idx, addr)
        runBlocking {
            addrDao.insert(addr)
        }
    }

    fun delete(addr: Address) {
        addresses().remove(addr)
        runBlocking {
            addrDao.delete(addr)
        }
    }

    fun getAddress(
        address: String,
        ticketTXID: String = "",
        delegate: AsyncObserver? = null
    ): Address {
        for (addr in addresses()) {
            if (addr.address == address || (addr.ticketTXID != "" && addr.ticketTXID == ticketTXID)) {
                delegate?.processFinished(addr, ctx)
                return addr
            }
        }
        return newAddress(address, ticketTXID, delegate, ctx)
    }
}