package com.joegruff.viacoinaddressscanner

import android.support.v4.app.Fragment

class GetAddressActivity:ReusableFragmentActivity() {
    override fun createFragment(): Fragment {
        return GetAddressFragment()
    }
}