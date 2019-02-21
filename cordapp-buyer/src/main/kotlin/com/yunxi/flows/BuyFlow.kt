package com.yunxi.flows

import co.paralleluniverse.fibers.Suspendable
import com.yunxi.contracts.GoodsContract
import com.yunxi.states.Goods
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
open class BuyFlow(val txState: Goods, val counterParty: Party): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction{
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val buyCommand = Command(GoodsContract.Commands.Buy(), txState.participants.map { it.owningKey })
        val newGoodsState = txState.copy((txState.participants - ourIdentity).first())
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(newGoodsState, GoodsContract.TRANSACTION_CONTRACT_ID!!)
                .addCommand(buyCommand)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        val sessions = listOf(initiateFlow(counterParty))
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedTx, sessions))
        return subFlow(FinalityFlow(allSignedTransaction))
    }
}

@InitiatedBy(BuyFlow::class)
class BuyFlowResponder(val responderFlowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(responderFlowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat{
                val outputState = stx.tx.outputs.single().data
                "This must be a goods state" using(outputState is Goods)
            }
        }
        subFlow(signTransactionFlow)
    }
}