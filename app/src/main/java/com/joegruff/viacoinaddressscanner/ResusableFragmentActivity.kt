package com.joegruff.viacoinaddressscanner

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity


abstract class ReusableFragmentActivity : FragmentActivity() {

    protected abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_container)

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragmentContainer)
        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
        }

    }
}
