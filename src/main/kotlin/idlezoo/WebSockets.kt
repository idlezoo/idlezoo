package idlezoo


import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableWebSocket
class WsConfiguration(private val gameWebSocketHandler: GameWebSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(gameWebSocketHandler, "/game/ws")
                .setAllowedOrigins("http://localhost:9000", "https://idlezoo.github.io")
                .withSockJS()

    }
}

@Component
class GameWebSocketHandler(private val gameService: GameService, private val fightService: FightService, private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(GameWebSocketHandler::class.java)

    private val wsSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val prev = wsSessions.put(session.principal!!.name, session)
        if (prev != null) {
            // TODO this is intended to close ws from prev session if new one is
            // opened
            // however it closes ws even for single session
            // prev.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        // TODO is this the intended way?
        val userId = ((session.principal as Authentication).principal as IdUser).id!!
        when (payload) {
            "ping" -> {
                // do nothing
            }
            "me" -> sendStateToPlayer(session, gameService.getZoo(userId))
            "fight" -> {
                val outcome = fightService.fight(userId)
                sendFightOutcome(session, outcome.outcome)
                sendStateToPlayer(session, gameService.getZoo(userId))
                when (outcome.outcome) {
                    Outcome.LOSS -> {
                        sendStateToPlayer(outcome.waitingFighter!!)
                        sendFightOutcome(outcome.waitingFighter, Outcome.WIN)
                    }
                    Outcome.WIN -> {
                        sendStateToPlayer(outcome.waitingFighter!!)
                        sendFightOutcome(outcome.waitingFighter, Outcome.LOSS)
                    }
                    else -> {
                    }
                }
            }
            else -> handleMessage(session, userId, payload)
        }
    }

    private fun handleMessage(session: WebSocketSession, userId: Int, payload: String) {
        if (payload.startsWith("buy/")) {
            val animal = payload.substring("buy/".length)
            val zoo = gameService.buy(userId, animal)
            sendStateToPlayer(session, zoo)
        } else if (payload.startsWith("upgrade/")) {
            val animal = payload.substring("upgrade/".length)
            val zoo = gameService.upgrade(userId, animal)
            sendStateToPlayer(session, zoo)
        } else if (payload.startsWith("buyPerk/")) {
            val perk = payload.substring("buyPerk/".length)
            val zoo = gameService.buyPerk(userId, perk)
            sendStateToPlayer(session, zoo)
        } else {
            throw IllegalStateException("Unkown message $payload")
        }

    }

    private fun sendStateToPlayer(zoo: Zoo) {
        val session = wsSessions[zoo.name]
        if (session != null) {
            sendStateToPlayer(session, zoo)
        }
    }

    private fun sendFightOutcome(zoo: Zoo, outcome: Outcome) {
        val session = wsSessions[zoo.name]
        if (session != null) {
            sendFightOutcome(session, outcome)
        }
    }

    private fun sendFightOutcome(session: WebSocketSession, outcome: Outcome) {
        try {
            session.sendMessage(TextMessage(outcome.toString()))
        } catch (e: IOException) {
            logger.info("Exception sending fight outcome", e)
            wsSessions.remove(session.principal!!.name)
        }

    }

    private fun sendStateToPlayer(session: WebSocketSession, zoo: Zoo) {
        try {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(zoo)))
        } catch (e: IOException) {
            logger.info("Exception sending player state", e)
            wsSessions.remove(session.principal!!.name)
        }

    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        wsSessions.remove(session.principal!!.name)
    }
}
