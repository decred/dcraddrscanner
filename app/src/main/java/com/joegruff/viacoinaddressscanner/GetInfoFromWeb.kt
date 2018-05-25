package com.joegruff.viacoinaddressscanner

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class GetInfoFromWeb(delegate:AsyncObserver,address: String) : AsyncTask<Void, Void, String>() {

    val API_URL = "https://explorer.viacoin.org/api/addr/"
    val add = address
    val del = delegate

    override fun doInBackground(vararg params: Void?): String {
        Log.d("asynctask", "doin in background")

        val url = URL(API_URL + add)
        val urlConnection = url.openConnection()
        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
        val line = bufferedReader.use { it.readText() }
        bufferedReader.close()
        return line
    }

    override fun onPostExecute(result: String?) {
        result?.let {
            Log.d("sdf", "on post execute"+result)
            val address = AddressObject(add)
            address.amount = result
            del.processfinished(result)
        }
        super.onPostExecute(result)
    }
}