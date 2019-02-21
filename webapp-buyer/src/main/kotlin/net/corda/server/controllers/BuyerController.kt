package net.corda.server.controllers

import com.yunxi.flows.BuyFlow
import com.yunxi.states.Goods
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import net.corda.client.jackson.JacksonSupport
import net.corda.server.NodeRPCConnection
import net.corda.server.models.GoodsInfo

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

    @PostMapping("/buy")
    private fun buy(@RequestBody info: GoodsInfo): ResponseEntity<String> {
        try {
            val tx = Goods(
                    owner = proxy.wellKnownPartyFromX500Name(CordaX500Name(info.owner, "London", "GB"))
                            ?: throw IllegalArgumentException("Unknown party name.")
            )
            println("1")
            val result = proxy.startFlowDynamic(BuyFlow::class.java, tx).returnValue.getOrThrow()
            println("2")
            return ResponseEntity.ok(mapper.writeValueAsString(result.coreTransaction.outputs))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("error:$e")
        }

    }
}