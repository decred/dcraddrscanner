package com.joegruff.decredaddressscanner.types

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException


const val NO_CONNECTION = "no_connection"

class GetInfoFromWeb(
    private val delegate: AsyncObserver,
    private val url: String,
    private val addr: String,
    private val newAddress: Boolean = false
) : ViewModel() {
    // Get data about address.
    private fun doInBackground(): String {
        var value = ""
        if (newAddress) {
            try {
                val url = URL(url + "address/" + addr)
                val urlConnection = url.openConnection()
                urlConnection.connectTimeout = 5000
                val bufferedReader =
                    BufferedReader(InputStreamReader(urlConnection.getInputStream()))
                val line = bufferedReader.use { it.readText() }
                bufferedReader.close()
                if (line == "Unprocessable Entity")
                    return ""
            } catch (e: Exception) {
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
            val url = URL(url + "address/" + addr + "/totals")
            val urlConnection = url.openConnection()
            urlConnection.connectTimeout = 5000
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            val line = bufferedReader.use { it.readText() }
            bufferedReader.close()
            return line

        } catch (e: Exception) {
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

    fun execute() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                try {
                    delegate.processBegan()
                    val result = doInBackground()
                    delegate.processFinished(result)
                    Log.d("loggering", "finished in get from web")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}