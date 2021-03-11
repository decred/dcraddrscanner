package com.decred.decredaddressscanner.activities


import androidx.fragment.app.Fragment
import com.decred.decredaddressscanner.viewfragments.ViewAddressFragment

class ViewAddressActivity : ReusableFragmentActivity() {
    override fun createFragment(): Fragment {
        val address = intent.getSerializableExtra(ViewAddressFragment.INTENT_ADDRESS_DATA) as String
        val ticketTXID =
            intent.getSerializableExtra(ViewAddressFragment.INTENT_TICKET_TXID_DATA) as String
        return ViewAddressFragment.new(address, ticketTXID)
    }
}