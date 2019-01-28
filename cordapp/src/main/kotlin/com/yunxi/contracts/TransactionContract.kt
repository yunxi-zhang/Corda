package com.yunxi.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import com.yunxi.states.Transaction

class TransactionContract: Contract {
    companion object {
        @JvmStatic
        val TRANSACTION_CONTRACT_ID = TransactionContract::class.qualifiedName
    }

    interface Commands: CommandData{
        class Sell: TypeOnlyCommandData(), Commands

    }

    override fun verify(tx: LedgerTransaction) {
        var command = tx.commands.requireSingleCommand<Commands>()
        val transaction = tx.outRefsOfType<Transaction>().last().state.data

        when (command.value) {
            is Commands.Sell -> requireThat {
                "Payment must be a positive number" using(transaction.payment > 0)
            }
        }
    }
}