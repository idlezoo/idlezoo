package idlemage.game.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Mage;
import idlemage.game.domain.MageBuildings;
import idlemage.game.domain.Mage.InsuffisientFundsException;
import idlemage.game.services.ResourcesService;
import idlemage.game.services.GameService;

@RestController
@RequestMapping("/game")
public class GameController {

  @Autowired
  private GameService gameService;

  @Autowired
  ResourcesService gameResources;

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

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InsuffisientFundsException.class)
  public String insuffisientFunds() {
    return "Not enough mana!";
  }
  
  public static class MageDTO {

    private final List<MageBuildings> buildings;
    private final double income;
    private final double mana;

    public MageDTO(Mage mage) {
      this.buildings = mage.getBuildings();
      this.income = mage.getIncome();
      this.mana = mage.getMana();
    }

    public double getMana() {
      return mana;
    }

    public double getManaIncome() {
      return income;
    }

    public List<MageBuildings> getBuildings() {
      return buildings;
    }

  }


}
