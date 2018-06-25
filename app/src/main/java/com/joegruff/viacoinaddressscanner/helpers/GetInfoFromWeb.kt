package com.joegruff.viacoinaddressscanner.helpers

import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*

const val NO_CONNECTION = "no_connection"

class GetInfoFromWeb(val delegate: AsyncObserver, val add: String) : AsyncTask<Void, Void, String>() {

    //this is the inspire api
    val API_URL = "https://explorer.viacoin.org/api/addr/"


    //get data about address
    override fun doInBackground(vararg params: Void?): String? {


        Log.d("async", "Doin in background")
        delegate.processbegan()
        try {
            val url = URL(API_URL + add)
            val urlConnection = url.openConnection()
            urlConnection.connectTimeout = 5000
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            val line = bufferedReader.use { it.readText() }
            bufferedReader.close()
            return line
        } catch (e:Exception){
            var value : String? = null
            when (e) {
                is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                    value = NO_CONNECTION
                else -> {
                }
            }
            return value
        }
    }

    //send back to address view fragment
    override fun onPostExecute(result: String?) {
        try {
            delegate.processfinished(result)
        } catch (e : Exception){
            e.printStackTrace()
        }

        super.onPostExecute(result)
    }
}