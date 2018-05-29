package com.joegruff.viacoinaddressscanner.activities

import android.support.v4.app.Fragment
import com.joegruff.viacoinaddressscanner.GetAddressFragment
import com.joegruff.viacoinaddressscanner.helpers.ReusableFragmentActivity

class GetAddressActivity: ReusableFragmentActivity() {
    override fun createFragment(): Fragment {
        return GetAddressFragment()
    }
}