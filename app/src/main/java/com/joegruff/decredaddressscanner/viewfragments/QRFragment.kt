package com.joegruff.decredaddressscanner.viewfragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.joegruff.decredaddressscanner.R
import com.journeyapps.barcodescanner.CaptureActivity

const val INTENT_INPUT_DATA = "joe.decred.address.scanner.input"

class QRFragment : Fragment(), OnRequestPermissionsResultCallback {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.qr_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestCameraPermission()
        } else {
            startCamera()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun requestCameraPermission() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            activity?.let {
                val allowed =
                    ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
                if (allowed == PackageManager.PERMISSION_DENIED) {
                    AlertDialog.Builder(it)
                        .setTitle(R.string.permission_camera_rationale)
                        .setMessage(R.string.permission_camera_rationale)
                        .setPositiveButton(
                            R.string.ok
                        ) { _, _ ->
                            val permissions =
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE)
                            ActivityCompat.requestPermissions(it, permissions, 200)
                        }
                        .setNegativeButton(R.string.no_thanks) { _, _ ->
                            it.finish()
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
        startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                startCamera()
            } else {
                activity?.let {
                    Toast.makeText(
                        it,
                        R.string.no_camera_permission,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    it.finish()
                }
            }
        }
    }

    private fun startCamera() {
        val integrator = IntentIntegrator(this.activity)
        integrator.captureActivity = CaptureActivity::class.java
        integrator.setOrientationLocked(false)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt(getString(R.string.scan_info))
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }
}