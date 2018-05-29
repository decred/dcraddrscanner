package com.joegruff.viacoinaddressscanner

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import com.joegruff.viacoinaddressscanner.barcodeReader.BarcodeCaptureActivity



class GetAddressFragment : android.support.v4.app.Fragment() {

    private val RC_BARCODE_CAPTURE = 9001
    private val TAG = "BarcodeMain"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.get_address_view, container, false)
        val scanButton = v.findViewById<Button>(R.id.get_address_view_scan_button)
        scanButton.setOnClickListener {
            val intent = Intent(activity, BarcodeCaptureActivity::class.java)
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true)
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false)

            startActivityForResult(intent, RC_BARCODE_CAPTURE)
        }
        val pasteButton = v.findViewById<Button>(R.id.get_address_view_paste_button)
        pasteButton.setOnClickListener {

            activity?.let { it1 ->
                val clipboard = it1.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                if (clipboard?.primaryClip?.getItemAt(0) != null) {
                    val address = clipboard.primaryClip.getItemAt(0).text.toString()
                    val intent = Intent(it1, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                    it1.startActivity(intent)
                } else
                    Toast.makeText(it1, R.string.get_address_fragment_no_clipboard_data, Toast.LENGTH_SHORT).show()
            }


        }

        return v
    }
}