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
class BuyerController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val mapper = JacksonSupport.createDefaultMapper(rpc.proxy)
}