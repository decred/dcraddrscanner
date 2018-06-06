package com.joegruff.viacoinaddressscanner

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.joegruff.viacoinaddressscanner.helpers.*
import kotlinx.android.synthetic.main.balance_swirl.*
import kotlinx.android.synthetic.main.view_address_view.*
import org.json.JSONObject
import org.json.JSONTokener

class ViewAddressFragment : Fragment(), AsyncObserver {
    companion object {
        const val INTENT_DATA = "joe.viacoin.address.scanner.address"
        fun new(address: String): ViewAddressFragment {
            val args = Bundle()
            args.putSerializable(INTENT_DATA, address)
            val fragment = ViewAddressFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var addressObject: AddressObject? = null
    var address = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        address = arguments?.getSerializable(INTENT_DATA) as String
        val v = inflater.inflate(R.layout.view_address_view, container, false)


        addressObject = AddressBook.getAddress(address)



        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (addressObject != null) {
            setupeditlabel()
            setupqrcode()
            setupaddressbutton()
            setinfoview()
        } else {
            addressObject = AddressBook.newObjectFromAddress(address)
        }
        addressObject?.delegate = this

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        if (addressObject!!.isValid){
            AddressBook.updateAddress(addressObject!!)
            AddressBook.saveAddressBook(activity)
        }
        super.onPause()
    }

    override fun processbegan() {
        balance_swirl_layout.processbegan()
    }

    override fun processfinished(output: String?) {
        Log.d("asdsadf", "output was null")
        balance_swirl_layout.processfinished(output)
        if (output == null) {
            view_address_view_address_button.setText(R.string.view_address_fragment_invalid_address)
            return
        }
        val token = JSONTokener(output).nextValue()
        if (token is JSONObject) {
            val addressString = token.getString("addrStr")
            val amountString = token.getString("balance")

            if (address == addressString) {

                if (!addressObject!!.hasBeenInitiated) {
                    addressObject?.amount = amountString.toDouble()
                    addressObject?.hasBeenInitiated = true
                    activity?.let {
                        AddressBook.saveAddressBook(it)
                        setupeditlabel()
                        setupqrcode()
                        setupaddressbutton()
                        setinfoview()
                    }
                }
            }

        }
    }


    fun setinfoview() {
        addressObject?.let {
            if (!it.isUpdating) {
                balance_swirl_progress_bar.alpha = 0f
            }
            balance_swirl_balance.text = it.amount.toString()
            balance_swirl_layout.setOnClickListener { v ->
                it.oneminuteupdate(address)
            }

        }
        if (!addressObject!!.isUpdating) {
            balance_swirl_progress_bar.alpha = 0f
        }

    }

    fun setupeditlabel() {
        view_address_view_label.setText(addressObject?.title)
        view_address_view_label.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                addressObject?.title = p0.toString()
            }

        })
    }

    fun setupaddressbutton() {
        view_address_view_address_button.setText(addressObject?.address)
    }

    fun setupqrcode() {
        try {
            val bitmap = textToQRBitmap(addressObject!!.address)
            view_address_view_qr_code.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    @Throws(WriterException::class)
    fun textToQRBitmap(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(Value, BarcodeFormat.QR_CODE, 500, 500, null)
        } catch (Illegalargumentexception: IllegalArgumentException) {
            return null
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)

        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, 500, 0, 0, matrixWidth, matrixHeight)
        return bitmap
    }
}

