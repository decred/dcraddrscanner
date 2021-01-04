package com.joegruff.decredaddressscanner.types

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException


const val NO_CONNECTION = "no_connection"

class GetInfoFromWeb(
    // The delegate is always an Address.
    private val addr: Address,
    private val ctx: Context,
) : ViewModel() {
    private val urlStr = UserSettings.get(ctx).url()
    private fun doInBackground() {
        getTicketInfo()
        val url = URL(urlStr + "address/" + addr.address + "/totals")
        addr.updateBalanceFromWebJSON(ctx, getGetResp(url))
    }

    private fun getGetResp(url: URL): String {
        val urlConnection = url.openConnection()
        urlConnection.connectTimeout = 5000
        val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
        val line = bufferedReader.use { it.readText() }
        bufferedReader.close()
        return line
    }

    private fun getTicketInfo() {
        // Nothing to do if this isn`t a stake commitment.
        if (addr.ticketTXID == "") return
        fun status(): TicketStatus = ticketStatusFromName(addr.ticketStatus)
        // Return if status is not expected to change any more.
        if (status().done()) return
        val txURL = URL(urlStr + "tx/" + addr.ticketTXID)
        val txToken = getGetResp(txURL)
        // If no address this is initiation.
        if (addr.address == "") {
            addr.initTicketFromWebJSON(txToken)
        }
        if (status() == TicketStatus.UNMINED || status() == TicketStatus.UNKNOWN) {
            if (!addr.checkTicketMinedWebJSON(txToken)) return
        }
        if (status() == TicketStatus.IMMATURE) {
            if (!addr.checkTicketLive()) return
        }
        // Ticket is live.
        val statusURL = URL(urlStr + "tx/" + addr.ticketTXID + "/tinfo")
        val webStatus = getGetResp(statusURL)
        if (addr.checkTicketVotedOrMissedWebJSON(webStatus))
            addr.checkTicketExpired()
    }

    fun execute() {
        GlobalScope.launch {
            try {
                addr.processBegan()
                doInBackground()
                addr.processFinished(ctx)
            } catch (e: Exception) {
                when (e) {
                    is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                        addr.processError(NO_CONNECTION)
                    else -> {
                        addr.processError(e.message ?: "unspecified error")
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}