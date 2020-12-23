package com.joegruff.decredaddressscanner.types

import android.content.Context
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
    // The delegate is always an Address.
    private val delegate: AsyncObserver,
    private val addr: String,
    private val ctx: Context,
) : ViewModel() {
    // Get data about address.
    private fun doInBackground(): Address {
        val urlStr = UserSettings.get(ctx).settings.url
        val url = URL(urlStr + "address/" + addr + "/totals")
        val urlConnection = url.openConnection()
        urlConnection.connectTimeout = 5000
        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
        val line = bufferedReader.use { it.readText() }
        bufferedReader.close()
        return addrFromWebJSON(line)
    }

    fun execute() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                try {
                    delegate.processBegan()
                    val result = doInBackground()
                    delegate.processFinished(result, ctx)
                } catch (e: Exception) {
                    when (e) {
                        is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                            delegate.processError(NO_CONNECTION)
                        else -> {
                            delegate.processError(e.message ?: "unspecified error")
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}