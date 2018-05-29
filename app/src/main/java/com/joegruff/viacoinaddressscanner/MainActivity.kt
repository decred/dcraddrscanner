package com.joegruff.viacoinaddressscanner

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.joegruff.viacoinaddressscanner.activities.GetAddressActivity
import com.joegruff.viacoinaddressscanner.activities.ViewAddressActivity
import com.joegruff.viacoinaddressscanner.helpers.AddressBook
import com.joegruff.viacoinaddressscanner.helpers.AddressObject

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    var adapter : MyAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        AddressBook.fillAddressBook(this)

        adapter = MyAdapter(this, R.layout.one_list_item_view, AddressBook.addresses)
        list_view.adapter = adapter
        list_view.setOnItemClickListener {parent, view, position, id ->
            val address = AddressBook.addresses[position].address
            val intent = Intent(this, ViewAddressActivity::class.java)
            intent.putExtra(ViewAddressFragment.INTENT_DATA, address)
            startActivity(intent)
        }


        fab.setOnClickListener { view ->
            val i = Intent(this, GetAddressActivity::class.java)
            this.startActivity(i)
        }
    }

    override fun onResume() {
        adapter?.notifyDataSetChanged()
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


    class MyAdapter(context:Context, resID: Int, objects : ArrayList<AddressObject>) : ArrayAdapter<AddressObject>(context, resID, objects) {
        val ctx = context
        val id = resID
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var holder = viewholder()
            var v = convertView
            if (v == null) {
                v = inflater.inflate(R.layout.one_list_item_view,parent,false)
                holder.textView = v.findViewById(R.id.one_list_item_view_text_view)
                v.setTag(holder)
            } else {
                holder = v.getTag() as viewholder
            }
            var string = AddressBook.addresses[position].title
            if (string == ""){
                string = AddressBook.addresses[position].address
            }
            holder.textView?.setText(string)
            Log.d("position","da position " + position + " " + AddressBook.addresses[position].address)

            return v!!
        }

        class viewholder {
            var textView: TextView? = null
        }
    }
}
