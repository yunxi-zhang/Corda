package com.yunxi.states

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

data class Transaction (
        val sellerBalance: Int,
        val buyerBalance: Int,
        val payment: Int,
        val seller: Party,
        val buyer: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        override val participants: List<Party> = listOf(seller, buyer)): LinearState {

}