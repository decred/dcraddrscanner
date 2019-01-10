package com.joegruff.decredaddressscanner.activities

import android.support.v4.app.Fragment
import com.joegruff.decredaddressscanner.ViewAddressFragment
import com.joegruff.decredaddressscanner.helpers.ReusableFragmentActivity

class ViewAddressActivity : ReusableFragmentActivity(){
    override fun createFragment(): Fragment {
        val address = intent.getSerializableExtra(ViewAddressFragment.INTENT_ADDRESS_DATA) as String
        return ViewAddressFragment.new(address)
    }
}