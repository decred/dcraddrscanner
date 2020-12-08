package com.joegruff.decredaddressscanner.helpers

import android.content.Context
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
            for (i in 0 until array.length()) {
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
        }
        ctx?.let {
            val file = getFile(it)
            if (!file.exists()) {
                file.createNewFile()
            }
            file.printWriter().use { it2 -> it2.println(jsonArray.toString()) }
        }
    }

    private fun getFile(ctx: Context): File {
        return File(ctx.filesDir.absolutePath + File.pathSeparator + "dcraddresses.json")
    }
}