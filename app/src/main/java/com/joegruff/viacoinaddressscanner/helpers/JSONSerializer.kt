package com.joegruff.viacoinaddressscanner.helpers

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONTokener
import java.io.File

object JSONSerializer {

    fun getAddresses(ctx: Context): ArrayList<AddressObject>? {
        val file = getFile(ctx)
        if (file.exists()) {
            val inputStream = file.inputStream()
            val jsonArray = inputStream.bufferedReader().use { it.readLine() }
            val arrayList = ArrayList<AddressObject>()
            val array = JSONTokener(jsonArray).nextValue() as JSONArray
            for (i in 0..(array.length() - 1)) {
                arrayList.add(AddressObject(array.getJSONObject(i)))
            }
            return arrayList

        }
        return null
    }

    fun saveJSON(ctx: Context?, arrayList: ArrayList<AddressObject>) {
        val jsonArray = JSONArray()
        for (c in arrayList) {
            jsonArray.put(c.toJSON())
            Log.d("sadf", c.toJSON().toString())
        }
        ctx?.let {
            val file = getFile(it)
            if (!file.exists()) {
                file.createNewFile()
            }

            file.printWriter().use { it2->it2.println(jsonArray.toString()) }

        }

    }

    fun getFile(ctx: Context): File {
        val file = File(ctx.filesDir.absolutePath + File.pathSeparator + "addresses.json")
        return file
    }
}