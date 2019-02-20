package net.corda.server.controllers

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId
import net.corda.client.jackson.JacksonSupport
import net.corda.server.NodeRPCConnection

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

    @GetMapping("/status", produces = arrayOf("text/plain"))
    private fun status() = "200"

    @GetMapping("/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping("/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping("/identities", produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping( "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping("/peers", produces = arrayOf("text/plain"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping("/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping( "/flows", produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping("/states", produces = arrayOf("text/plain"))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()
}