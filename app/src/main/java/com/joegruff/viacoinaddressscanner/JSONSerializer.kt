package com.joegruff.viacoinaddressscanner

import android.content.Context
import org.json.JSONArray
import org.json.JSONTokener
import java.io.File
import java.io.InputStream

object JSONSerializer {

    fun getAddresses(ctx: Context): ArrayList<AddressObject>? {
        val file = getFile(ctx)
        if (file.exists()) {
            val inputStream = file.inputStream()
            val jsonArray = inputStream.bufferedReader().use { it.readText() }
            val arrayList = ArrayList<AddressObject>()
            val array = JSONTokener(jsonArray).nextValue() as JSONArray
            for (i in 0..(array.length() - 1)) {
                arrayList.add(AddressObject(array.getJSONObject(i)))
            }
            return arrayList

        }
        return null
    }

    fun saveJSON(ctx: Context, arrayList: ArrayList<AddressObject>) {
        var jsonArray = JSONArray()
        for (c in arrayList) {
            jsonArray.put(c.toString())
        }
        val file = getFile(ctx)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.outputStream().bufferedWriter().use { it.newLine() }
    }

    fun getFile(ctx: Context): File {
        val file = File(ctx.filesDir.absolutePath + File.pathSeparator + "addresses.json")
        return file
    }
}