package com.joegruff.viacoinaddressscanner.helpers

import org.json.JSONObject

class AddressObject {
    val JSON_ADDRESS:String = "address"
    val JSON_TITLE = "title"
    val JSON_AMOUNT = "amount"

    var address = ""
    var title = ""
    var amount = 0.0

    constructor(jsonObject: JSONObject){
        address = jsonObject.getString(JSON_ADDRESS)
        title = jsonObject.getString(JSON_TITLE)
        amount = jsonObject.getDouble(JSON_AMOUNT)
    }
    constructor()

    fun toJSON() : JSONObject{
        val jsonObject = JSONObject()
        jsonObject.put(JSON_ADDRESS,address)
        jsonObject.put(JSON_TITLE,title)
        jsonObject.put(JSON_AMOUNT,amount)
        return jsonObject
    }
}