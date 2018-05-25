package com.joegruff.viacoinaddressscanner

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class GetAddressFragment:android.support.v4.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.get_address_view,container,false)
        val scanButton = v.findViewById<Button>(R.id.get_address_view_scan_button)
        scanButton.setOnClickListener {

        }
        val pasteButton = v.findViewById<Button>(R.id.get_address_view_paste_button)
        pasteButton.setOnClickListener {

            activity?.let { val clipboard = it.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val address = clipboard.primaryClip.getItemAt(0).text.toString()
                val intent = Intent(it,ViewAddressActivity::class.java)
                intent.putExtra(ViewAddressFragment.INTENT_DATA,address)
                it.startActivity(intent)
            }


        }

        return v
    }
}