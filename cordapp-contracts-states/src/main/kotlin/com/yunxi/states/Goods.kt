package com.yunxi.states

import com.yunxi.contracts.GoodsContract
import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.OwnableState
import net.corda.core.identity.AbstractParty

data class Goods (override val owner: AbstractParty,
                  val name: String = "car",
                  val type: String = "luxary"): OwnableState{

    override val participants: List<AbstractParty> = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty) = CommandAndState(GoodsContract.Commands.Move(), copy(owner = newOwner))

}