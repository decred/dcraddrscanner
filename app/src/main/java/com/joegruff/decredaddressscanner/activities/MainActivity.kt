package com.joegruff.decredaddressscanner.activities

import android.app.Activity
import android.app.AlarmManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.joegruff.decredaddressscanner.R
import com.joegruff.decredaddressscanner.types.*
import com.joegruff.decredaddressscanner.viewfragments.INTENT_INPUT_DATA
import com.joegruff.decredaddressscanner.viewfragments.ViewAddressFragment
import org.json.JSONArray
import java.util.concurrent.CountDownLatch

var RC_BARCODE_CAPTURE = 9001

class MainActivity : SwipeRefreshLayout.OnRefreshListener, AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var menuItems: MenuItems

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        this.menuItems = MenuItems(this)
        val ptr = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh_layout)
        ptr.setOnRefreshListener(this)

        setRepeatingAlarm(this, AlarmManager.INTERVAL_HALF_HOUR)
        viewManager = LinearLayoutManager(this)

        viewAdapter = MyAdapter(this, AddressBook.get(this).addresses())

        recyclerView = findViewById<RecyclerView>(R.id.recycle_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addItemDecoration(SimpleDividerItemDecoration())

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as MyAdapter
                adapter.onItemRemove(viewHolder, recyclerView)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val fab: View = findViewById(R.id.fab)

        fab.setOnClickListener {
            val dialogView = BottomSheetDialog(this)
            dialogView.setContentView(R.layout.get_address_view)
            val qrButton = dialogView.findViewById<Button>(R.id.get_address_view_scan_button)
            val pasteButton = dialogView.findViewById<Button>(R.id.get_address_view_paste_button)
            qrButton?.setOnClickListener { _ ->
                dialogView.dismiss()
                val intent = Intent(this, QRActivity::class.java)
                this.startActivityForResult(intent, RC_BARCODE_CAPTURE)
            }
            pasteButton?.setOnClickListener { _ ->
                val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard.primaryClip?.getItemAt(0) != null) {
                    val input = clipboard.primaryClip?.getItemAt(0)?.text.toString()
                    val intent = Intent(this, ViewAddressActivity::class.java)
                    var addrStr = input
                    var ticketTxid = ""
                    if (input.length == 64) {
                        addrStr = ""
                        ticketTxid = input
                    }
                    intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, addrStr)
                    intent.putExtra(ViewAddressFragment.INTENT_TICKET_TXID_DATA, ticketTxid)
                    this.startActivity(intent)
                } else
                    Toast.makeText(
                        this,
                        R.string.get_address_fragment_no_clipboard_data,
                        Toast.LENGTH_SHORT
                    ).show()
                dialogView.dismiss()
            }
            dialogView.show()
        }
    }

    private fun updateAddresses(force: Boolean) {
            val addrs = AddressBook.get(this).addresses()
            for (addr in addrs) {
                if (force) addr.update(this) else addr.updateIfFiveMinPast(this)
            }
    }

    override fun onRefresh() {
        val ptr = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh_layout)
        ptr.isRefreshing = false
        updateAddresses(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(applicationContext, ViewAddressActivity::class.java)
            var input = data?.getStringExtra(INTENT_INPUT_DATA) ?: ""
            val splitInput = input.split(":")
            input = splitInput[splitInput.lastIndex]
            input = input.trim()
            try {
                val token = JSONArray(input)
                waitForAddresses(token)
                viewAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                // Expected if not a json array.
            }
            var addrStr = input
            var ticketTxid = ""
            if (input.length == 64) {
                addrStr = ""
                ticketTxid = input
            }
            intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, addrStr)
            intent.putExtra(ViewAddressFragment.INTENT_TICKET_TXID_DATA, ticketTxid)
            this.startActivity(intent)
        }

    }

    class Del(private val latch: CountDownLatch) : AsyncObserver {
        override fun processFinished(addr: Address, ctx: Context) {
            latch.countDown()
        }

        override fun processBegan() {}
        override fun processError(str: String) {
            latch.countDown()
        }
    }

    private fun waitForAddresses(token: JSONArray) {
        val n = token.length()
        val latch = CountDownLatch(n)
        val book = AddressBook.get(this)
        for (i in 0 until n) {
            val addr = book.getAddress("", token[i] as String, Del(latch))
            addr.isBeingWatched = true
        }
        latch.await()
    }

    override fun onResume() {
        updateAddresses(false)
        synchronized(viewAdapter.haveTouchedAnAddress) {
            viewAdapter.haveTouchedAnAddress = false
        }
        viewAdapter.notifyDataSetChanged()
        super.onResume()
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuItems.prepareOptionsMenu(menu, this)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (this.menuItems.optionsItemSelected(item.itemId, this)) return true
        return super.onOptionsItemSelected(item)
    }


    class MyAdapter(private val ctx: Context, private var addresses: ArrayList<Address>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        @Volatile
        var haveTouchedAnAddress = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val mainView = LayoutInflater.from(parent.context)
                .inflate(R.layout.one_list_item_view, parent, false)
            return MyViewHolder(mainView)
        }

        override fun getItemCount(): Int {
            return addresses.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val addr = addresses[position]
            synchronized(holder.delegateHolder) {
                addr.delegates.updateIgnoreNull(holder.delegateHolder, null, null)
                holder.delegateHolder.abbreviatedValues = true
                holder.delegateHolder.setUI(addr)
            }
            var title = addr.title
            if (title == "") {
                title = addr.address
            }
            holder.textView.text = title
            holder.itemView.setOnClickListener {
                synchronized(haveTouchedAnAddress) {
                    if (!haveTouchedAnAddress) {
                        haveTouchedAnAddress = true
                        val intent = Intent(ctx, ViewAddressActivity::class.java)
                        intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, addr.address)
                        intent.putExtra(
                            ViewAddressFragment.INTENT_TICKET_TXID_DATA,
                            addr.ticketTXID
                        )
                        ctx.startActivity(intent)
                    }
                }
            }
        }

        fun onItemRemove(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {
            val adapterPosition = viewHolder.adapterPosition
            val addr = addresses[adapterPosition]
            val book = AddressBook.get(this.ctx)
            val snackbar = Snackbar
                .make(recyclerView, R.string.main_view_deleted_address, Snackbar.LENGTH_LONG)
                .setAction(R.string.main_view_undo_delete) {
                    book.insert(addr, adapterPosition)
                    notifyItemInserted(adapterPosition)
                    recyclerView.scrollToPosition(adapterPosition)
                }
            snackbar.show()
            book.delete(addr)
            notifyItemRemoved(adapterPosition)
        }


        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.one_list_item_view_text_view)
            @Volatile
            var delegateHolder: MyConstraintLayout =
                itemView.findViewById(R.id.balance_swirl_layout)
        }
    }

    inner class SimpleDividerItemDecoration : RecyclerView.ItemDecoration() {
        private var mDivider: Drawable? =
            ResourcesCompat.getDrawable(resources, R.drawable.line_divider, null)

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

            val childCount = parent.childCount
            for (i in 0 until childCount) {
                val child = parent.getChildAt(i)

                val params = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + params.bottomMargin
                mDivider?.let {
                    val bottom = top + it.intrinsicHeight

                    it.setBounds(left, top, right, bottom)
                    it.draw(c)
                }

            }
        }
    }

    abstract class SwipeToDeleteCallback(context: Context) :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        private val deleteIcon = getDrawable(context, R.drawable.ic_delete_white_24)

        private val background = ColorDrawable()
        private val backgroundColor = Color.parseColor("#f44336")
        private val clearPaint =
            Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (deleteIcon == null) return
            val intrinsicWidth = deleteIcon.intrinsicWidth
            val intrinsicHeight = deleteIcon.intrinsicHeight
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(
                    c,
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }

            // Draw the red delete background
            background.color = backgroundColor
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // Calculate position of delete icon
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            // Draw the delete icon
            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }


        private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
            c?.drawRect(left, top, right, bottom, clearPaint)
        }
    }
}


