package com.joegruff.decredaddressscanner.viewfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.joegruff.decredaddressscanner.R
import com.joegruff.decredaddressscanner.types.*

class ViewAddressFragment : Fragment(), AsyncObserver {
    companion object {
        const val INTENT_ADDRESS_DATA = "joe.decred.address.scanner.address"
        const val INTENT_TICKET_TXID_DATA = "joe.decred.address.scanner.ticket.txid"
        fun new(address: String, ticketTxid: String = ""): ViewAddressFragment {
            val args = Bundle()
            args.putSerializable(INTENT_ADDRESS_DATA, address)
            args.putSerializable(INTENT_TICKET_TXID_DATA, ticketTxid)
            val fragment = ViewAddressFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var address: Address
    private var isInitiated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val addrStr = arguments?.getSerializable(INTENT_ADDRESS_DATA) as String
        val ticketStr = arguments?.getSerializable(INTENT_TICKET_TXID_DATA) as String
        val v = inflater.inflate(R.layout.view_address_view, container, false)
        address = AddressBook.get(context!!).getAddress(addrStr, ticketStr)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (address.isValid) {
            setupEditLabel()
            setupQRCode()
            setupAddressButton()
            setupInfoView()
            setupWatchStar()
            isInitiated = true
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        address.delegates.addrFragment = this
        if (address.isValid) {
            address.updateIfFiveMinPast(context!!)
        }
        super.onResume()
    }

    override fun onPause() {
        AddressBook.get(context!!).updateAddress(address, false)
        super.onPause()
    }

    override fun processBegan() {}

    override fun processError(str: String) {
        if (!isInitiated) {
            val addrButton =
                this.activity!!.findViewById<TextView>(R.id.view_address_view_address_button)
            if (str == NO_CONNECTION) {
                addrButton.setText(R.string.view_address_fragment_no_connection)
                return
            }
            addrButton.setText(R.string.view_address_fragment_invalid_address)
        }
        this.activity!!.runOnUiThread {
            Toast.makeText(
                this.activity,
                str,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun processFinished(addr: Address, ctx: Context) {
        if (address.isValid) {
            if (!isInitiated) {
                isInitiated = true
                activity.let {
                    this.activity!!.runOnUiThread {
                        setupEditLabel()
                        setupQRCode()
                        setupAddressButton()
                        setupInfoView()
                        setupWatchStar()
                    }
                }
            }
        }
    }

    private fun setupInfoView() {
        val swirlLayout =
            this.activity!!.findViewById<MyConstraintLayout>(R.id.balance_swirl_layout)
        swirlLayout.setAmounts(
            address.amount.toString(),
            address.amountOld.toString()
        )
        swirlLayout.setTicketStatus(address.ticketStatus)
        swirlLayout.setOnClickListener {
            address.update(context!!)
        }
        this.address.delegates.swirl = swirlLayout
    }

    private fun setupWatchStar() {
        checkStar(address)
        val starButton =
            this.activity!!.findViewById<Button>(R.id.view_address_view_address_star_button)
        starButton.setOnClickListener {
            address.isBeingWatched = !address.isBeingWatched
            val messageId =
                if (address.isBeingWatched) R.string.updates_on else R.string.updates_off
            val name = if (address.title == "") address.address else address.title
            val message = getString(messageId) + " " + name
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            checkStar(address)
        }
    }

    private fun checkStar(addr: Address) {
        val id =
            if (addr.isBeingWatched) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        val starButton =
            this.activity!!.findViewById<Button>(R.id.view_address_view_address_star_button)
        starButton.background = ActivityCompat.getDrawable(this.context!!, id)
    }

    private fun setupEditLabel() {
        val addrLabel = this.activity!!.findViewById<EditText>(R.id.view_address_view_label)
        addrLabel.setText(address.title)
        addrLabel.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                address.title = p0.toString()
            }
        })
    }

    private fun setupAddressButton() {
        val addrButton = this.activity!!.findViewById<Button>(R.id.view_address_view_address_button)
        addrButton.text = address.address
        addrButton.setOnClickListener {
            val clipboard =
                activity?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("address", address.address)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(
                activity,
                R.string.view_address_fragment_copied_clipdata,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupQRCode() {
        try {
            val bitmap = textToQRBitmap(address.address)
            val qrImg = this.activity!!.findViewById<ImageView>(R.id.view_address_view_qr_code)
            qrImg.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    @Throws(WriterException::class)
    fun textToQRBitmap(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(Value, BarcodeFormat.QR_CODE, 500, 500, null)
        } catch (illegalArgumentException: IllegalArgumentException) {
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

