package com.joegruff.decredaddressscanner.activities


import android.content.Intent
import androidx.fragment.app.Fragment
import com.joegruff.decredaddressscanner.helpers.QRFragment
import com.joegruff.decredaddressscanner.helpers.ReusableFragmentActivity

class QRActivity : ReusableFragmentActivity() {
    private var frag: QRFragment? = null
    override fun createFragment(): Fragment {
        frag = QRFragment.new()
        return frag!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        // Camera will call here.
        frag!!.cameraCallback(requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }
}