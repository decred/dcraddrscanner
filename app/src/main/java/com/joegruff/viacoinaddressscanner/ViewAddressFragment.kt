package com.joegruff.viacoinaddressscanner

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class ViewAddressFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.view_address_view,container,false)

        val imageview = v.findViewById<ImageView>(R.id.view_address_view_qr_code)

        val infoview = v.findViewById<TextView>(R.id.view_address_view_info)


        val observer = Observer<String>{
            AddressBook.currentAddress?.let { infoview.setText(it.address + "\n" + it.amount) }
        }

        return v
    }
}