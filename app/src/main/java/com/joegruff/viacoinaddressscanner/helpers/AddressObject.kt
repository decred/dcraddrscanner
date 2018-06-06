package com.joegruff.viacoinaddressscanner.helpers

import android.location.Address
import android.os.Handler
import android.util.Log
import com.joegruff.viacoinaddressscanner.R
import org.json.JSONObject
import org.json.JSONTokener

class AddressObject : AsyncObserver {
    val JSON_ADDRESS: String = "address"
    val JSON_TITLE = "title"
    val JSON_AMOUNT = "amount"

    var address = ""
    var title = ""
    var amount = 0.0
    var isUpdating = false
    var updatePosted = false
    var isValid = false
    var delegate : AsyncObserver? = null
    var hasBeenInitiated = false


    constructor(jsonObject: JSONObject) {
        address = jsonObject.getString(JSON_ADDRESS)
        title = jsonObject.getString(JSON_TITLE)
        amount = jsonObject.getDouble(JSON_AMOUNT)
        hasBeenInitiated = true
        isValid = true
        oneminuteupdate(address)
    }

    constructor(add : String){
        oneminuteupdate(add)
    }

    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(JSON_ADDRESS, address)
        jsonObject.put(JSON_TITLE, title)
        jsonObject.put(JSON_AMOUNT, amount)
        return jsonObject
    }

    override fun processbegan() {
        try {
            delegate?.processbegan()
        } catch (e:Exception){

        }

    }
    fun oneminuteupdate(add:String){
        if (!isUpdating) {
            isUpdating = true
            GetInfoFromWeb(this, add).execute()
        }
    }

    override fun processfinished(output: String?) {
        try {
            delegate?.processfinished(output)
        } catch (e:Exception){

        }
        if (output != null) {
            val token = JSONTokener(output).nextValue()
            if (token is JSONObject) {
                val addressString = token.getString("addrStr")
                val amountString = token.getString("balance")
                if (address == addressString) {
                        amount = amountString.toDouble()
                        Log.d("addressobject", "process finished "+ output)

                    if (!updatePosted) {
                        Handler().postDelayed({
                            GetInfoFromWeb(this, address).execute()
                            try {this.updatePosted = false} catch (e:Exception){Log.d("postingblock", "was no object left")}
                            Log.d("update was posted ","return value " + updatePosted.toString())
                        }, 60000)
                        updatePosted = true
                    }
                    isValid = true
                }

            }
        }
        isUpdating = false
    }
}