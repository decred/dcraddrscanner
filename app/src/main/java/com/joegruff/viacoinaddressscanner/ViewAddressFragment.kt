package com.joegruff.viacoinaddressscanner

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class ViewAddressFragment : Fragment(),AsyncObserver{
    companion object {
        val INTENT_DATA = "joe.viacoin.address.scanner.address"
        fun new(address: String):ViewAddressFragment{
            val args = Bundle()
            args.putSerializable(INTENT_DATA, address)
            val fragment = ViewAddressFragment()
            fragment.setArguments(args)
            return fragment
        }
    }
    var infoview : TextView? = null
    var address : String = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        address = arguments?.getSerializable(INTENT_DATA) as String
        val v = inflater.inflate(R.layout.view_address_view,container,false)

        GetInfoFromWeb(this,address).execute()

        val imageview = v.findViewById<ImageView>(R.id.view_address_view_qr_code)

        infoview = v.findViewById(R.id.view_address_view_info)

        return v
    }

    override fun processfinished(output: String) {
        Log.d("asdf", "made it to process finish"+ AddressBook.currentAddress?.amount)
        infoview?.setText(output)
    }
}

interface AsyncObserver{
    fun processfinished(output:String)
}