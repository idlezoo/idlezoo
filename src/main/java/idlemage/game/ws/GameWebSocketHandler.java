package idlemage.game.ws;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import idlemage.game.domain.MageDTO;
import idlemage.game.services.FightService;
import idlemage.game.services.GameService;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

	private final ConcurrentHashMap<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();
	private final GameService gameService;
	private final FightService fightService; 
	private final ObjectMapper objectMapper;

	public GameWebSocketHandler(GameService gameService, FightService fightService, ObjectMapper objectMapper) {
		this.gameService = gameService;
		this.fightService=fightService;
		this.objectMapper=objectMapper;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		wsSessions.put(session.getPrincipal().getName(), session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message)
			throws Exception {
		String payload = message.getPayload();
		String user = session.getPrincipal().getName();
		switch (payload) {
		case "fight":
			String enemy = fightService.fight(user);
			break;

		default:
			break;
		}
	}
	
	private void sendStateToPlayer(String username){
		WebSocketSession session = wsSessions.get(username);
		if(session == null){
			return;
		}
		new MageDTO(gameService.getMage(username));
	}
	

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
			throws Exception {
		wsSessions.remove(session.getPrincipal().getName());
	}

}
