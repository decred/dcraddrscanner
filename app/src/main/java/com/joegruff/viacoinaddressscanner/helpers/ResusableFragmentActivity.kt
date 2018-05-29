package com.joegruff.viacoinaddressscanner.helpers

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.joegruff.viacoinaddressscanner.R


abstract class ReusableFragmentActivity : FragmentActivity() {

    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_container)

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragmentContainer)
        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        }

    }
}
