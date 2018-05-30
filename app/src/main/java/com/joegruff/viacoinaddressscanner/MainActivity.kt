package com.joegruff.viacoinaddressscanner

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import com.joegruff.viacoinaddressscanner.activities.GetAddressActivity
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import com.joegruff.viacoinaddressscanner.helpers.AddressBook
import com.joegruff.viacoinaddressscanner.helpers.AddressObject

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        AddressBook.fillAddressBook(this)


        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(AddressBook.addresses)

        recyclerView = findViewById<RecyclerView>(R.id.recycle_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }



        /*recycle_view.setOnItemClickListener {parent, view, position, id ->
            val address = AddressBook.addresses[position].address
            val intent = Intent(this, ViewAddressActivity::class.java)
            intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
            startActivity(intent)
        }*/


        fab.setOnClickListener { view ->
            val i = Intent(this, GetAddressActivity::class.java)
            this.startActivity(i)
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


    class MyAdapter(private val myDataset: ArrayList<AddressObject>) : RecyclerView.Adapter<MyAdapter.viewholder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
            val mainView = LayoutInflater.from(parent.context).inflate(R.layout.one_list_item_view,parent,false) as FrameLayout
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
        }

        class viewholder(itemview: FrameLayout) : RecyclerView.ViewHolder(itemview) {
            val textView = itemView.findViewById<TextView>(R.id.one_list_item_view_text_view)
        }
    }
}
