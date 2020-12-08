package com.joegruff.decredaddressscanner.helpers

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*

const val NO_CONNECTION = "no_connection"

class GetInfoFromWeb(
    private val delegate: AsyncObserver,
    private val add: String,
    private val newAddress: Boolean = false
) : AsyncTask<Void, Void, String>() {

    // This is the dcrdata api.
    private val API_URL = "https://explorer.dcrdata.org/api/address/"


    // Get data about address.
    override fun doInBackground(vararg params: Void?): String? {
        delegate.processBegan()
        if (newAddress) {
            try {
                val url = URL(API_URL + add)
                val urlConnection = url.openConnection()
                urlConnection.connectTimeout = 5000
                val bufferedReader =
                    BufferedReader(InputStreamReader(urlConnection.getInputStream()))
                val line = bufferedReader.use { it.readText() }
                bufferedReader.close()
                if (line == "Unprocessable Entity")
                    return null
            } catch (e: Exception) {
                var value: String? = null
                when (e) {
                    is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                        value = NO_CONNECTION
                    else -> {
                        e.printStackTrace()
                    }
                }
                return value
            }
        }
        try {
            val url = URL("$API_URL$add/totals")
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
                    e.printStackTrace()
                }
            }
            return value
        }

    }

    // Send back to address view fragment.
    override fun onPostExecute(result: String?) {
        try {
            delegate.processFinished(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onPostExecute(result)
    }
}