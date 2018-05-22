package com.joegruff.viacoinaddressscanner

import org.json.JSONObject

class AddressObject {
    val JSON_ADDRESS:String = "address"
    val JSON_TITLE = "title"
    val JSON_AMOUNT = "amount"
    var JSON_VALID = "valid"

    var address = ""
    var title = ""
    var amount = .0
    var valid = false

    constructor(jsonObject: JSONObject){
        address = jsonObject.getString(JSON_ADDRESS)
        title = jsonObject.getString(JSON_TITLE)
        amount = jsonObject.getDouble(JSON_AMOUNT)
        valid = jsonObject.getBoolean(JSON_VALID)
    }
    constructor(constructoraddress:String){
        address = constructoraddress
    }
}