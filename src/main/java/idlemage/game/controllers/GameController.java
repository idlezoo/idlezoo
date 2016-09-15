package idlemage.game.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Mage.InsuffisientFundsException;
import idlemage.game.domain.MageDTO;
import idlemage.game.services.FightService;
import idlemage.game.services.GameService;
import idlemage.game.services.ResourcesService;

@RestController
@RequestMapping("/game")
public class GameController {

  @Autowired
  private GameService gameService;

  @Autowired
  private ResourcesService gameResources;
  @Autowired
  private FightService fightService;

  @RequestMapping("/me")
  public MageDTO me(Principal user) {
    return new MageDTO(gameService.getMage(user.getName()).updateMana());
  }

  @RequestMapping("/me/mana")
  public double mana(Principal user) {
    return me(user).getMana();
  }

  @RequestMapping("/buy")
  public MageDTO buy(Principal user, String building) {
    return new MageDTO(gameService.getMage(user.getName()).buy(building, gameResources));
  }

  @RequestMapping("/upgrade")
  public MageDTO upgrade(Principal user, String building) {
    return new MageDTO(gameService.getMage(user.getName()).upgrade(building));
  }

  @RequestMapping("/fight")
  public MageDTO fight(Principal user) {
    fightService.fight(user.getName());
    return me(user);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InsuffisientFundsException.class)
  public String insuffisientFunds() {
    return "Not enough mana!";
  }


}
