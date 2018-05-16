package idlezoo.game.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WsConfiguration implements WebSocketConfigurer {
    private final GameWebSocketHandler gameWebSocketHandler;

    public WsConfiguration(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/game/ws")
                .setAllowedOrigins("http://localhost:9000", "https://idlezoo.github.io")
                .withSockJS();

    }
}
