package idlemage.game.ws;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import idlemage.game.domain.Mage;
import idlemage.game.domain.MageDTO;
import idlemage.game.services.FightService;
import idlemage.game.services.GameService;
import idlemage.game.services.ResourcesService;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

  private final ConcurrentHashMap<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();
  @Autowired
  private GameService gameService;
  @Autowired
  private FightService fightService;
  @Autowired
  private ResourcesService resourcesService;
  @Autowired
  private ObjectMapper objectMapper;

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
      case "ping":
        //do nothing
        break;
      case "me":
        sendStateToPlayer(user);
        break;
      case "fight":
        Mage enemy = fightService.fight(user);
        sendStateToPlayer(user);
        if (enemy != null) {
          sendStateToPlayer(enemy);
        }
        break;
      default:
        handleMessage(session, user, payload);
    }
  }

  private void handleMessage(WebSocketSession session,String user, String payload) {
    if (payload.startsWith("buy/")) {
      String creature = payload.substring("buy/".length());
      Mage mage = gameService.getMage(user).buy(creature, resourcesService);
      sendStateToPlayer(session, mage);
    }else if(payload.startsWith("upgrade/")){
      String creature = payload.substring("upgrade/".length());
      Mage mage = gameService.getMage(user).upgrade(creature);
      sendStateToPlayer(session, mage);
    }else{
      throw new IllegalStateException("Unkown message " + payload);
    }

  }

  private void sendStateToPlayer(String username) {
    WebSocketSession session = wsSessions.get(username);
    if (session == null) {
      return;
    }
    sendStateToPlayer(session, gameService.getMage(username));
  }

  private void sendStateToPlayer(Mage mage) {
    WebSocketSession session = wsSessions.get(mage.getName());
    if (session != null) {
      sendStateToPlayer(session, mage);
    }
  }

  private void sendStateToPlayer(WebSocketSession session, Mage mage) {
    MageDTO mageDTO = new MageDTO(mage.updateMana());
    try {
      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(mageDTO)));;
    } catch (IOException e) {
      wsSessions.remove(mage.getName());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
      throws Exception {
    wsSessions.remove(session.getPrincipal().getName());
  }

}
