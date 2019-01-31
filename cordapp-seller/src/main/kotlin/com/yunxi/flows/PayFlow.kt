package com.yunxi.flows

import co.paralleluniverse.fibers.Suspendable
import com.yunxi.contracts.TransactionContract
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import com.yunxi.states.Transaction

@InitiatingFlow
@StartableByRPC
open class PayFlow1(val txState: Transaction): FlowLogic<SignedTransaction>() {


    @Suspendable
    override fun call(): SignedTransaction{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val sellCommand = Command(TransactionContract.Commands.Sell(), txState.participants.map { it.owningKey })
        val newTxState = txState.copy(
                sellerBalance = txState.sellerBalance + txState.payment,
                buyerBalance = txState.buyerBalance - txState.payment
        )
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(newTxState, TransactionContract.TRANSACTION_CONTRACT_ID!!)
                .addCommand(sellCommand)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        val sessions = (txState.participants - ourIdentity).map { initiateFlow(it) }
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedTx, sessions))
        return subFlow(FinalityFlow(allSignedTransaction))
    }
}

@InitiatedBy(PayFlow1::class)
class PayFlowResponder1(val responderFlowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(responderFlowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat{
                val outputState = stx.tx.outputs.single().data
                "This must be a transaction state" using(outputState is Transaction)
            }
        }
        subFlow(signTransactionFlow)
    }
}