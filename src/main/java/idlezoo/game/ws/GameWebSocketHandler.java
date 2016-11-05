package idlezoo.game.ws;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import idlezoo.game.domain.ZooDTO;
import idlezoo.game.services.FightService;
import idlezoo.game.services.GameService;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

  private final ConcurrentHashMap<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();

  @Autowired
  private GameService gameService;
  @Autowired
  private FightService fightService;
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    WebSocketSession prev = wsSessions.put(session.getPrincipal().getName(), session);
    if (prev != null) {
      // TODO this is intended to close ws from prev session if new one is opened
      // however it closes ws even for single session
      // prev.close(CloseStatus.POLICY_VIOLATION);
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message)
      throws Exception {
    String payload = message.getPayload();
    String user = session.getPrincipal().getName();
    switch (payload) {
      case "ping":
        // do nothing
        break;
      case "me":
        sendStateToPlayer(session, gameService.getZoo(user));
        break;
      case "fight":
        ZooDTO enemy = fightService.fight(user);
        sendStateToPlayer(session, gameService.getZoo(user));
        if (enemy != null) {
          sendStateToPlayer(enemy);
        }
        break;
      default:
        handleMessage(session, user, payload);
    }
  }

  private void handleMessage(WebSocketSession session, String user, String payload) {
    if (payload.startsWith("buy/")) {
      String animal = payload.substring("buy/".length());
      ZooDTO zoo = gameService.buy(user, animal);
      sendStateToPlayer(session, zoo);
    } else if (payload.startsWith("upgrade/")) {
      String animal = payload.substring("upgrade/".length());
      ZooDTO zoo = gameService.upgrade(user, animal);
      sendStateToPlayer(session, zoo);
    } else {
      throw new IllegalStateException("Unkown message " + payload);
    }

  }

  private void sendStateToPlayer(ZooDTO zoo) {
    WebSocketSession session = wsSessions.get(zoo.getName());
    if (session != null) {
      sendStateToPlayer(session, zoo);
    }
  }


  private void sendStateToPlayer(WebSocketSession session, ZooDTO zoo) {
    try {
      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(zoo)));;
    } catch (IOException e) {
      wsSessions.remove(session.getPrincipal().getName());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
      throws Exception {
    wsSessions.remove(session.getPrincipal().getName());
  }

}
