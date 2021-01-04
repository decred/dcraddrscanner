package com.joegruff.decredaddressscanner.types

val UNKNOWN_NETWORK = Exception("unknown network")
val UNKNOWN_ADDRESS_TYPE = Exception("unknown address type")

enum class Net(
    val Name: String,
    val DefaultPort: String,
    val NetworkAddressPrefix: String,
    val PubKeyAddrID: String,
    val PubKeyHashAddrID: String,
    val PKHEdwardsAddrID: String,
    val PKHSchnorrAddrID: String,
    val ScriptHashAddrID: String,
    val TicketMaturity: Int,
    val TicketExpiry: Int,
    val TargetTimePerBlock: Int, // Seconds
) {
    Mainnet(
        Name = "mainnet",
        DefaultPort = "9108",
        NetworkAddressPrefix = "D",
        PubKeyAddrID = "Dk",
        PubKeyHashAddrID = "Ds",
        PKHEdwardsAddrID = "De",
        PKHSchnorrAddrID = "DS",
        ScriptHashAddrID = "Dc",
        TicketMaturity = 256,
        TicketExpiry = 40960,
        TargetTimePerBlock = 300,
    ),
    Testnet(
        Name = "testnet3",
        DefaultPort = "19108",
        NetworkAddressPrefix = "T",
        PubKeyAddrID = "Tk",
        PubKeyHashAddrID = "Ts",
        PKHEdwardsAddrID = "Te",
        PKHSchnorrAddrID = "TS",
        ScriptHashAddrID = "Tc",
        TicketMaturity = 16,
        TicketExpiry = 6144,
        TargetTimePerBlock = 120,
    ),
    Simnet(
        Name = "simnet",
        DefaultPort = "18555",
        NetworkAddressPrefix = "S",
        PubKeyAddrID = "     Sk",
        PubKeyHashAddrID = " Ss",
        PKHEdwardsAddrID = " Se",
        PKHSchnorrAddrID = " SS",
        ScriptHashAddrID = " Sc",
        TicketMaturity = 16,
        TicketExpiry = 384,
        TargetTimePerBlock = 1,
    );
}

fun netFromName(name: String): Net {
    return when (name) {
        Net.Mainnet.Name -> Net.Mainnet
        Net.Testnet.Name -> Net.Testnet
        Net.Simnet.Name -> Net.Simnet
        else -> throw UNKNOWN_NETWORK
    }
}

fun netFromAddr(addr: String): Net {
    // TODO: Be more precise.
    if (addr.length < 30) throw UNKNOWN_ADDRESS_TYPE
    val prefix = addr.slice(0..1)
    val net = when (prefix.slice(0..0)) {
        Net.Mainnet.NetworkAddressPrefix -> Net.Mainnet
        Net.Testnet.NetworkAddressPrefix -> Net.Testnet
        Net.Simnet.NetworkAddressPrefix -> Net.Simnet
        else -> throw UNKNOWN_NETWORK
    }
    if (
        prefix != net.PubKeyAddrID
        && prefix != net.PubKeyHashAddrID
        && prefix != net.PKHEdwardsAddrID
        && prefix != net.PKHSchnorrAddrID
        && prefix != net.ScriptHashAddrID
    ) throw UNKNOWN_ADDRESS_TYPE
    return net
}