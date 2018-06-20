package com.joegruff.viacoinaddressscanner

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import com.joegruff.viacoinaddressscanner.barcodeReader.BarcodeCaptureActivity
import com.joegruff.viacoinaddressscanner.helpers.AddressBook
import com.joegruff.viacoinaddressscanner.helpers.AddressObject
import com.joegruff.viacoinaddressscanner.helpers.AsyncObserver
import com.joegruff.viacoinaddressscanner.helpers.MyConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val RC_BARCODE_CAPTURE = 9001
    private val TAG = "BarcodeMain"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        AddressBook.fillAddressBook(this)


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

        recyclerView.addItemDecoration(SimpleDividerItemDecoration(this))

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as MyAdapter
                adapter.onItemRemove(viewHolder,recyclerView)
                //adapter.asktoremove(viewHolder.adapterPosition)
                //adapter.removeAt(viewHolder.adapterPosition)
                Log.d("dfdf", "swiped to delete")
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)


        fab.setOnClickListener { view ->
            val dialogview = BottomSheetDialog(this)
            dialogview.setContentView(R.layout.get_address_view)
            val qrbutton = dialogview.findViewById<Button>(R.id.get_address_view_scan_button)
            val pastebutton = dialogview.findViewById<Button>(R.id.get_address_view_paste_button)
            qrbutton?.setOnClickListener {
                val intent = Intent(this, BarcodeCaptureActivity::class.java)
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true)
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false)
                this.startActivityForResult(intent, RC_BARCODE_CAPTURE)
                dialogview.dismiss()
            }
            pastebutton?.setOnClickListener {
                val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                if (clipboard?.primaryClip?.getItemAt(0) != null) {
                    val address = clipboard.primaryClip.getItemAt(0).text.toString()
                    val intent = Intent(this, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                    this.startActivity(intent)
                } else
                    Toast.makeText(this, R.string.get_address_fragment_no_clipboard_data, Toast.LENGTH_SHORT).show()

                dialogview.dismiss()
            }
            dialogview.show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_BARCODE_CAPTURE && resultCode == Activity.RESULT_OK){
            val intent = Intent(applicationContext, ViewAddressActivity::class.java)
            intent.putExtra(ViewAddressFragment.INTENT_DATA, data?.getStringExtra(ViewAddressFragment.INTENT_DATA))
            this.startActivity(intent)
        }
    }

    override fun onPause() {
        AddressBook.saveAddressBook(this)
        super.onPause()
    }

    override fun onResume() {
        viewAdapter.notifyDataSetChanged()
        Log.d("num", "num of addresses " + AddressBook.addresses.size)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    class MyAdapter(private val ctx: Context, private val myDataset: ArrayList<AddressObject>) : RecyclerView.Adapter<MyAdapter.viewholder>() {

        val addressesToDelete = ArrayList<AddressObject>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
            val mainView = LayoutInflater.from(parent.context).inflate(R.layout.one_list_item_view, parent, false)
            return viewholder(mainView)
        }

        override fun getItemCount(): Int {
            return myDataset.size
        }

        override fun onBindViewHolder(holder: viewholder, position: Int) {
            var string = myDataset[position].title
            if (string == "") {
                string = myDataset[position].address
            }
            holder.textView.text = string
            holder.delegateHolder.abbreviatedValues = true
            holder.delegateHolder.setAmount(myDataset[position].amount.toString())
            holder.itemView.setOnClickListener {
                val address = myDataset[position].address
                val intent = Intent(ctx, ViewAddressActivity::class.java)
                intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                ctx.startActivity(intent)
            }
            myDataset[position].delegate = holder.delegateHolder
        }


        fun onItemRemove(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {
            val adapterPosition = viewHolder.adapterPosition
            Log.d("asdsadf", "adapter position "+ adapterPosition)
            val addressObject = myDataset.get(adapterPosition)
            val snackbar = Snackbar
                    .make(recyclerView, "PHOTO REMOVED", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", {view->
                            //val mAdapterPosition = viewHolder.adapterPosition
                            myDataset.add(adapterPosition, addressObject)
                            notifyItemInserted(adapterPosition)
                            recyclerView.scrollToPosition(adapterPosition)
                            addressesToDelete.remove(addressObject)

                    })
            snackbar.show()
            myDataset.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
            addressesToDelete.add(addressObject)
        }



        class viewholder(itemview: View) : RecyclerView.ViewHolder(itemview){
            val textView = itemview.findViewById<TextView>(R.id.one_list_item_view_text_view)
            val balanceTextview = itemview.findViewById<TextView>(R.id.balance_swirl_balance)
            val progressBar = itemview.findViewById<ProgressBar>(R.id.balance_swirl_progress_bar)
            val changeView = itemview.findViewById<TextView>(R.id.balance_swirl_change)
            val delegateHolder = itemview.findViewById<MyConstraintLayout>(R.id.balance_swirl_layout)
        }
    }

    inner class SimpleDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private var mDivider: Drawable?

        init {
            mDivider = ResourcesCompat.getDrawable(resources,R.drawable.line_divider,null)
        }

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

    abstract class SwipeToDeleteCallback(val context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)!!
        private val intrinsicWidth = deleteIcon.intrinsicWidth
        private val intrinsicHeight = deleteIcon.intrinsicHeight
        private val background = ColorDrawable()
        private val backgroundColor = Color.parseColor("#f44336")
        private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }




        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            return false
        }


        override fun onChildDraw(
                c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive



            if (isCanceled) {
                clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }



            // Draw the red delete background
            background.color = backgroundColor
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
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
