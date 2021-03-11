package com.decred.decredaddressscanner.viewfragments

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
import com.decred.decredaddressscanner.R
import com.decred.decredaddressscanner.types.*

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

    var address: Address = Address("placeholder")

    @Volatile
    private var isInitiated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val addrStr = arguments?.getSerializable(INTENT_ADDRESS_DATA) as String
        val ticketStr = arguments?.getSerializable(INTENT_TICKET_TXID_DATA) as String
        context?.let { address = AddressBook.get(it).getAddress(addrStr, ticketStr) }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_address_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up the view if the address is already known to be valid. Otherwise it will be set up
        // upon address.update() completing.
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
        address.delegates.updateIgnoreNull(null, this, null)
        if (address.isValid) {
            context?.let { address.updateIfFiveMinPast(it) }
        }
        super.onResume()
    }

    override fun onPause() {
        if (address.isValid) {
            // Updates isBeingWatched and the title.
            context?.let { AddressBook.get(it).update(address) }
        }
        super.onPause()
    }

    override fun processBegin() {}

    // processError will show an error in the form of UI changes if not isInitiated, or as a toast
    // if already initiated.
    override fun processError(err: String) {
        if (synchronized(isInitiated) { !isInitiated }) {
            val addrButton =
                this.activity?.findViewById<TextView>(R.id.view_address_view_address_button)
            if (err == NO_CONNECTION) {
                addrButton?.setText(R.string.view_address_fragment_no_connection)
                return
            }
            addrButton?.setText(R.string.view_address_fragment_invalid_address)
        }
        this.activity?.runOnUiThread {
            Toast.makeText(
                this.activity,
                err,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun processFinish(addr: Address, ctx: Context) {
        if (address.isValid) {
            synchronized(isInitiated) {
                if (!isInitiated) {
                    isInitiated = true
                    this.activity?.runOnUiThread {
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
            this.activity?.findViewById<MyConstraintLayout>(R.id.balance_swirl_layout)
        swirlLayout?.setAmounts(
            address.amount.toString(),
            address.amountOld.toString()
        )
        swirlLayout?.setTicketStatus(address.ticketStatus)
        swirlLayout?.setOnClickListener {
            context?.let { address.update(it) }
        }
        this.address.delegates.updateIgnoreNull(swirlLayout, null, null)
    }

    private fun setupWatchStar() {
        checkStar(address)
        val starButton =
            this.activity?.findViewById<Button>(R.id.view_address_view_address_star_button)
        starButton?.setOnClickListener {
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
            this.activity?.findViewById<Button>(R.id.view_address_view_address_star_button)
        context?.let { starButton?.background = ActivityCompat.getDrawable(it, id) }
    }

    private fun setupEditLabel() {
        val addrLabel = this.activity?.findViewById<EditText>(R.id.view_address_view_label)
        addrLabel?.setText(address.title)
        addrLabel?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                address.title = p0.toString()
            }
        })
    }

    private fun setupAddressButton() {
        val addrButton = this.activity?.findViewById<Button>(R.id.view_address_view_address_button)
        addrButton?.text = address.address
        addrButton?.setOnClickListener {
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
            val qrImg = this.activity?.findViewById<ImageView>(R.id.view_address_view_qr_code)
            qrImg?.setImageBitmap(bitmap)
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

