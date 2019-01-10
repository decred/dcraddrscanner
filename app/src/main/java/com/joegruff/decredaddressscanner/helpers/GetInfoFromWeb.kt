package com.joegruff.decredaddressscanner.helpers

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*

const val NO_CONNECTION = "no_connection"

class GetInfoFromWeb(val delegate: AsyncObserver, val add: String, val newAddress : Boolean = false) : AsyncTask<Void, Void, String>() {

    //this is the inspire api
    val API_URL = "https://explorer.dcrdata.org/api/address/"


    //get data about address
    override fun doInBackground(vararg params: Void?): String? {

        Log.d("async", "Doin in background")
        delegate.processbegan()

        if (newAddress) {
            Log.d("arrrg ","not in boog")
            try {
                val url = URL(API_URL + add)
                val urlConnection = url.openConnection()
                urlConnection.connectTimeout = 5000
                val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
                val line = bufferedReader.use { it.readText() }
                bufferedReader.close()
                //we dont make it here...
                Log.d("arrrg ",line)
                if (line == "Unprocessable Entity")
                return null

            } catch (e: Exception) {
                var value: String? = null
                when (e) {
                    is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                        value = NO_CONNECTION
                    else -> {
                    }
                }
                return value
            } finally {

            }
        }
            try {
                val url = URL(API_URL + add + "/totals")
                val urlConnection = url.openConnection()
                urlConnection.connectTimeout = 5000
                val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
                val line = bufferedReader.use { it.readText() }
                bufferedReader.close()
                return line

            } catch (e: Exception) {
                var value: String? = null
                when (e) {
                    is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                        value = NO_CONNECTION
                    else -> {
                    }
                }
                return value
            } finally {

            }

    }

    //send back to address view fragment
    override fun onPostExecute(result: String?) {
        Log.d("async", "onpostexecute "+ result)
        try {
            delegate.processfinished(result)
        } catch (e : Exception){
            e.printStackTrace()
        }

        super.onPostExecute(result)
    }
}