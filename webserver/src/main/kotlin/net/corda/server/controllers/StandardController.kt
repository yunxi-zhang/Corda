package net.corda.server.controllers

import com.yunxi.flows.PayFlow1
import com.yunxi.states.Transaction
import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.server.NodeRPCConnection
import net.corda.server.models.TransactionInfo
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId
import net.corda.client.jackson.JacksonSupport

/**
 * A CorDapp-agnostic controller that exposes standard endpoints.
 */
@RestController
@RequestMapping("/") // The paths for GET and POST requests are relative to this base path.
class StandardController(
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

    @GetMapping(value = "/status", produces = arrayOf("text/plain"))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf("text/plain"))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()
}