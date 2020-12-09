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
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.joegruff.decredaddressscanner.helpers.*

var RC_BARCODE_CAPTURE = 9001

class MainActivity : SwipeRefreshLayout.OnRefreshListener, AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: MyAdapter
    private lateinit var viewManager: LinearLayoutManager
    private val TAG = "BarcodeMain"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        AddressBook.fillAddressBook(this)

        val ptr = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh_layout)
        ptr.setOnRefreshListener(this)
        setRepeatingAlarm(this, AlarmManager.INTERVAL_HALF_HOUR)


        viewManager = LinearLayoutManager(this)

        viewAdapter = MyAdapter(this, AddressBook.addresses)

        recyclerView = findViewById<RecyclerView>(R.id.recycle_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
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

                    val address = clipboard.primaryClip?.getItemAt(0)?.text.toString()
                    val intent = Intent(this, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, address)
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

    override fun onRefresh() {
        Log.d(TAG, "refreshing")
        Handler(Looper.getMainLooper()).postDelayed({
            val ptr = findViewById<SwipeRefreshLayout>(R.id.pullToRefresh_layout)
            ptr.isRefreshing = false
        }, 1000)
        AddressBook.updateAddresses(true)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(applicationContext, ViewAddressActivity::class.java)
            var address = data?.getStringExtra(ViewAddressFragment.INTENT_ADDRESS_DATA) ?: ""
            val splitAddress = address.split(":")
            address = splitAddress[splitAddress.lastIndex]
            address = address.trim()

            intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, address)
            this.startActivity(intent)
        }

    }


    override fun onPause() {
        AddressBook.saveAddressBook(this)
        super.onPause()
    }

    override fun onResume() {
        Log.d("num", "num of addresses " + AddressBook.addresses.size)
        AddressBook.updateAddresses()
        viewAdapter.haveTouchedAnAddress = false
        viewAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            // Put options here.
            else -> super.onOptionsItemSelected(item)
        }


    }


    class MyAdapter(private val ctx: Context, val myDataset: ArrayList<AddressObject>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private val addressesToDelete = ArrayList<AddressObject>()
        var haveTouchedAnAddress = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val mainView = LayoutInflater.from(parent.context)
                .inflate(R.layout.one_list_item_view, parent, false)
            return MyViewHolder(mainView)
        }


        override fun getItemCount(): Int {
            return myDataset.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            myDataset[position].delegates.set(0, holder.delegateHolder)

            val address = myDataset[position].address
            var string = myDataset[position].title
            if (string == "") {
                string = address
            }
            //holder.textView.text = position.toString()
            holder.delegateHolder.myAddress = address
            holder.textView.text = string
            holder.delegateHolder.abbreviatedValues = true
            holder.delegateHolder.setAmounts(
                myDataset[position].amount.toString(),
                myDataset[position].amountOld.toString()
            )
            holder.itemView.setOnClickListener {
                if (!haveTouchedAnAddress) {
                    haveTouchedAnAddress = true
                    val intent = Intent(ctx, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_ADDRESS_DATA, address)
                    ctx.startActivity(intent)
                }
            }
        }

        //after a cell is swiped for delete
        fun onItemRemove(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {
            val adapterPosition = viewHolder.adapterPosition
            val addressObject = myDataset.get(adapterPosition)
            val snackbar = Snackbar
                .make(recyclerView, R.string.main_view_deleted_address, Snackbar.LENGTH_LONG)
                .setAction(R.string.main_view_undo_delete) {
                    myDataset.add(adapterPosition, addressObject)
                    notifyItemInserted(adapterPosition)
                    recyclerView.scrollToPosition(adapterPosition)
                    addressesToDelete.remove(addressObject)
                }
            snackbar.show()
            myDataset.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
            addressesToDelete.add(addressObject)
        }


        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.one_list_item_view_text_view)
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

        private val deleteIcon = getDrawable(context, R.drawable.ic_delete_white_24)!!
        private val intrinsicWidth = deleteIcon.intrinsicWidth
        private val intrinsicHeight = deleteIcon.intrinsicHeight
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


