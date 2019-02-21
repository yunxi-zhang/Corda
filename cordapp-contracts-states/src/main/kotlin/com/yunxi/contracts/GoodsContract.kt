package com.yunxi.contracts

import com.yunxi.states.Goods
import com.yunxi.states.Transaction
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction

class GoodsContract: Contract {

    companion object {
        @JvmStatic
        val TRANSACTION_CONTRACT_ID = TransactionContract::class.qualifiedName
    }

    interface Commands: CommandData {
        class Move: TypeOnlyCommandData(), Commands
        class Buy: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        var command = tx.commands.requireSingleCommand<Commands>()
        val buyTx = tx.outRefsOfType<Goods>().last().state.data

        when (command.value) {
            is Commands.Buy -> requireThat {
                "Name must be a car" using(buyTx.name == "car")
            }
        }
    }
}