package com.joegruff.viacoinaddressscanner

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import com.joegruff.viacoinaddressscanner.barcodeReader.BarcodeCaptureActivity
import com.joegruff.viacoinaddressscanner.helpers.AddressBook
import com.joegruff.viacoinaddressscanner.helpers.AddressObject

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat


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



        fab.setOnClickListener { view ->
            val dialogview = BottomSheetDialog(this)
            dialogview.setContentView(R.layout.get_address_view)
            val qrbutton = dialogview.findViewById<Button>(R.id.get_address_view_scan_button)
            val pastebutton = dialogview.findViewById<Button>(R.id.get_address_view_paste_button)
            qrbutton?.setOnClickListener {
                val intent = Intent(this, BarcodeCaptureActivity::class.java)
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true)
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false)

                startActivityForResult(intent, RC_BARCODE_CAPTURE)
                dialogview.dismiss()
            }
            pastebutton?.setOnClickListener {
                val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                if (clipboard?.primaryClip?.getItemAt(0) != null) {
                    val address = clipboard.primaryClip.getItemAt(0).text.toString()
                    val intent = Intent(this, ViewAddressActivity::class.java)
                    intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                    startActivity(intent)
                } else
                    Toast.makeText(this, R.string.get_address_fragment_no_clipboard_data, Toast.LENGTH_SHORT).show()

                dialogview.dismiss()
            }
            dialogview.show()

        }
    }


    override fun onResume() {
        //adapter?.notifyDataSetChanged()
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
            holder.textView.setText(string)
            holder.itemView.setOnClickListener {
                val address = myDataset[position].address
                val intent = Intent(ctx, ViewAddressActivity::class.java)
                intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
                ctx.startActivity(intent)
            }
        }

        class viewholder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            val textView = itemView.findViewById<TextView>(R.id.one_list_item_view_text_view)
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


}
