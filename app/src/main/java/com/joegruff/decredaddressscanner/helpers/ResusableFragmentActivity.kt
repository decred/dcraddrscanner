package com.joegruff.decredaddressscanner.helpers

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.joegruff.decredaddressscanner.R


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
