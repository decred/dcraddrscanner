package com.joegruff.decredaddressscanner.types

import android.content.Context
import kotlinx.coroutines.runBlocking

private var addresses: ArrayList<Address>? = null

// AddressBook should always be called with the get method. It will create a new instance if none
// exists or return an existing one.
class AddressBook(private val addrDao: AddressDao) {
    companion object {
        @Volatile
        private var addrBook: AddressBook? = null
        fun get(
            ctx: Context,
        ): AddressBook {
            return addrBook ?: synchronized(this) {
                val db = MyDatabase.get(ctx)
                val instance = AddressBook(db.addrDao())
                addrBook = instance
                instance
            }
        }
    }

    // addresses will populate addresses if not already populated. Care must be taken to ensure that
    // the ArrayList and values in the database are in sync. Currently, addresses are inserted or
    // updated in the database individually upon address.update()
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

    // update expects that addr is already an element in addresses.
    fun update(addr: Address) {
        runBlocking {
            addrDao.update(addr)
        }
    }

    // insert should only be called on a new address not yet in addresses.
    fun insert(addr: Address, idx: Int = addresses().size) {
        addresses().add(idx, addr)
        runBlocking {
            addrDao.insert(addr)
        }
    }

    // delete is called when swiping an address from MainActivity.
    fun delete(addr: Address) {
        addresses().remove(addr)
        runBlocking {
            addrDao.delete(addr)
        }
    }

    // getAddress retrieves an address from addresses based upon a matching address or matching
    // txid. If not yet in addresses a new address is returned with the async update process already
    // started. Whether or not the address is added to addresses depends upon the delegate's
    // processFinish (added) or processError (not added).
    fun getAddress(
        ctx: Context,
        address: String,
        ticketTXID: String = "",
        delegate: AsyncObserver? = null
    ): Address {
        for (addr in addresses()) {
            if (addr.address == address || (addr.ticketTXID != "" && addr.ticketTXID == ticketTXID)) {
                delegate?.processFinish(addr, ctx)
                return addr
            }
        }
        return newAddress(address, ticketTXID, delegate, ctx)
    }
}