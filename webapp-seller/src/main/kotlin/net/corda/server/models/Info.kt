package net.corda.server.models

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionInfo (
        @JsonProperty("sellerBalance")
        val sellerBalance: Int,
        @JsonProperty("buyerBalance")
        val buyerBalance: Int,
        @JsonProperty("payment")
        val payment: Int,
        @JsonProperty("seller")
        val seller: String,
        @JsonProperty("buyer")
        val buyer: String
        )