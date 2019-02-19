package net.corda.server.controllers

import com.yunxi.flows.PayFlow1
import com.yunxi.states.Transaction
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.server.models.TransactionInfo
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import net.corda.client.jackson.JacksonSupport
import net.corda.server.NodeRPCConnection

/**
 * A CorDapp-agnostic controller that exposes standard endpoints.
 */
@RestController
@RequestMapping("/") // The paths for GET and POST requests are relative to this base path.
class SellerController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val mapper = JacksonSupport.createDefaultMapper(rpc.proxy)

    @PostMapping("/pay")
    private fun pay(@RequestBody info: TransactionInfo): ResponseEntity<String> {
        try {
            val tx = Transaction(
                    sellerBalance = info.sellerBalance,
                    buyerBalance = info.buyerBalance,
                    payment = info.payment,
                    seller = proxy.wellKnownPartyFromX500Name(CordaX500Name(info.seller, "London", "GB"))
                            ?: throw IllegalArgumentException("Unknown party name."),
                    buyer = proxy.wellKnownPartyFromX500Name(CordaX500Name(info.buyer, "London", "GB"))
                            ?: throw IllegalArgumentException("Unknown party name.")
            )
            val result = proxy.startFlowDynamic(PayFlow1::class.java, tx).returnValue.getOrThrow()
            return ResponseEntity.ok(mapper.writeValueAsString(result))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("error:$e")
        }

    }
}