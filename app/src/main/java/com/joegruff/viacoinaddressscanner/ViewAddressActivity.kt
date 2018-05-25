package com.joegruff.viacoinaddressscanner

import android.support.v4.app.Fragment

class ViewAddressActivity : ReusableFragmentActivity(){
    override fun createFragment(): Fragment {
        val address = intent.getSerializableExtra(ViewAddressFragment.INTENT_DATA) as String
        return ViewAddressFragment.new(address)
    }
}