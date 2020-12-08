package com.joegruff.decredaddressscanner.helpers

import com.joegruff.decredaddressscanner.R
import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
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
import com.journeyapps.barcodescanner.CaptureActivity

class QRFragment : Fragment(), OnRequestPermissionsResultCallback {
    companion object {
        fun new(): QRFragment {
            return QRFragment()
        }
    }

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

    fun cameraCallback(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
            if (scanResult.contents != null) {
                val address = scanResult.contents
                intent!!.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, address)
                this.activity?.setResult(RESULT_OK, intent)
                this.activity?.finish()
            } else {
                this.activity?.finish()
            }
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            val allowed =
                ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.CAMERA)
            if (allowed == PackageManager.PERMISSION_DENIED) {
                AlertDialog.Builder(this.context!!).setTitle(R.string.permission_camera_rationale)
                    .setMessage(R.string.permission_camera_rationale)
                    .setPositiveButton(
                        R.string.ok
                    ) { _, _ ->
                        val permissions =
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.VIBRATE)
                        ActivityCompat.requestPermissions(this.activity!!, permissions, 200)
                    }
                    .setNegativeButton(R.string.no_thanks) { _, _ ->
                        this.activity?.finish()
                    }
                    .setCancelable(false)
                    .show()
            }
            startCamera()
        }
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
                Toast.makeText(this.context!!, R.string.no_camera_permission, Toast.LENGTH_SHORT)
                    .show()
                this.activity?.finish()
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