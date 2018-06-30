package com.joegruff.viacoinaddressscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.joegruff.viacoinaddressscanner.helpers.*
import kotlinx.android.synthetic.main.balance_swirl.*
import kotlinx.android.synthetic.main.view_address_view.*
import org.json.JSONObject
import org.json.JSONTokener
import java.text.DecimalFormat

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
    var delegate : AsyncObserver? = null

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
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        addressObject?.delegate = this
        super.onResume()
    }

    override fun onPause() {
        if (addressObject!!.isValid){
            AddressBook.updateAddress(addressObject!!)
            AddressBook.saveAddressBook(activity)
        }
        super.onPause()
    }

    override fun processbegan() {
        try {
            delegate?.processbegan()
        } catch (e:Exception){

        }
    }

    override fun processfinished(output: String?) {
        try {
            delegate?.processfinished(output)
        } catch (e:Exception){

        }
        if (output == null) {
            view_address_view_address_button.setText(R.string.view_address_fragment_invalid_address)
            return
        }
        if (output == NO_CONNECTION){
            if (!addressObject!!.hasBeenInitiated){
                view_address_view_address_button.setText(R.string.view_address_fragment_no_connection)
                return
            }
        }
        if (addressObject!!.isValid) {
            if (!addressObject!!.hasBeenInitiated) {
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


    fun setinfoview() {
        addressObject?.let {
            val f = DecimalFormat("#.################")
            balance_swirl_balance.text = f.format(it.amount).toString()
            balance_swirl_balance.setOnClickListener { v ->
                it.update()
            }
            this.delegate = balance_swirl_layout

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
        view_address_view_address_button.setOnClickListener {
            val clipboard = activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("address",addressObject?.address)
            clipboard?.primaryClip = clip
            Toast.makeText(activity,R.string.view_address_fragment_copied_clipdata,Toast.LENGTH_SHORT).show()
        }
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

