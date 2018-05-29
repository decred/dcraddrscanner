package com.joegruff.viacoinaddressscanner.helpers

import android.os.AsyncTask
import android.util.Log
import com.joegruff.viacoinaddressscanner.AsyncObserver
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class GetInfoFromWeb(delegate: AsyncObserver, address: String) : AsyncTask<Void, Void, String>() {

    //this is the inspire api
    val API_URL = "https://explorer.viacoin.org/api/addr/"
    val INVALID_ADDRESS = "invalid address"
    val add = address
    val del = delegate

    //get data about address
    override fun doInBackground(vararg params: Void?): String? {
        try {
            val url = URL(API_URL + add)
            val urlConnection = url.openConnection()
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            val line = bufferedReader.use { it.readText() }
            bufferedReader.close()
            return line
        } catch (e:Exception){
            return null
        }
    }

    //send back to address view fragment
    override fun onPostExecute(result: String?) {
        result?.let {
            Log.d("sdf", "on post execute"+result)
        }
        del.processfinished(result)
        super.onPostExecute(result)
    }
}