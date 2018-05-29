package com.joegruff.viacoinaddressscanner

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.joegruff.viacoinaddressscanner.helpers.AddressBook
import com.joegruff.viacoinaddressscanner.helpers.AddressObject
import com.joegruff.viacoinaddressscanner.helpers.GetInfoFromWeb
import org.json.JSONObject
import org.json.JSONTokener

class ViewAddressFragment : Fragment(), AsyncObserver {
    companion object {
        val INTENT_DATA = "joe.viacoin.address.scanner.address"
        fun new(address: String): ViewAddressFragment {
            val args = Bundle()
            args.putSerializable(INTENT_DATA, address)
            val fragment = ViewAddressFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    var infoview: TextView? = null
    var addressbutton: Button? = null
    var imageview: ImageView? = null
    var labeledittext: EditText? = null
    var addressObject = AddressObject()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        addressObject.address = arguments?.getSerializable(INTENT_DATA) as String
        val v = inflater.inflate(R.layout.view_address_view, container, false)

        GetInfoFromWeb(this, addressObject.address).execute()
        AddressBook.currentAddress = addressObject.address

        imageview = v.findViewById(R.id.view_address_view_qr_code)
        infoview = v.findViewById(R.id.view_address_view_info)
        addressbutton = v.findViewById(R.id.view_address_view_address_button)
        labeledittext = v.findViewById(R.id.view_address_view_label)

        return v
    }

    override fun onPause() {
        AddressBook.updateAddress(addressObject)
        AddressBook.saveAddressBook(activity)
        super.onPause()
    }

    override fun processfinished(output: String?) {
        if (output == null) {
            addressbutton?.setText(R.string.view_address_fragment_invalid_address)
            return
        }
        val token = JSONTokener(output).nextValue()
        if (token is JSONObject) {
            val addressString = token.getString("addrStr")
            val amountString = token.getString("balance")
            setupeditlabel()

            if (AddressBook.currentAddress == addressString) {
                addressbutton?.setText(addressString)
                infoview?.setText(amountString)
                if (AddressBook.addAddressAndIsNewAddress(addressObject)) {
                    activity?.let { AddressBook.saveAddressBook(it) }
                } else {
                    AddressBook.getAddress(addressObject.address)?.let {
                        addressObject = it
                        labeledittext?.setText(it.title)
                    }
                }
                addressObject.amount = amountString.toDouble()

            }
        }


    }

    fun setupeditlabel() {
        labeledittext?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                addressObject.title = p0.toString()
            }

        })
    }
}

interface AsyncObserver {
    fun processfinished(output: String?)
}