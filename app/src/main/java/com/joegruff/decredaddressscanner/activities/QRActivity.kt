package com.joegruff.decredaddressscanner.activities


import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.joegruff.decredaddressscanner.viewfragments.QRFragment
import com.joegruff.decredaddressscanner.viewfragments.INTENT_INPUT_DATA

class QRActivity : ReusableFragmentActivity() {
    override fun createFragment(): Fragment {
        return QRFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        // Camera will call here.
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
            if (scanResult.contents != null) {
                val address = scanResult.contents
                intent?.putExtra(INTENT_INPUT_DATA, address)
                this.setResult(RESULT_OK, intent)
                this.finish()
            } else {
                this.finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }
}