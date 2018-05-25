package com.joegruff.viacoinaddressscanner

import android.support.v4.app.Fragment

class ViewAddressActivity : ReusableFragmentActivity(){
    override fun createFragment(): Fragment {
        return ViewAddressFragment()
    }
}