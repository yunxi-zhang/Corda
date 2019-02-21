package net.corda.server.models

import com.fasterxml.jackson.annotation.JsonProperty

data class GoodsInfo (
        @JsonProperty("owner")
        val owner: String
)