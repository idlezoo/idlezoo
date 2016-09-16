package idlemage.game.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Mage;
import idlemage.game.domain.MageBuildings;
import idlemage.game.services.GameService;
import idlemage.security.UsersService;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@RestController
@RequestMapping("/admin")
public class AdminController {

  @Autowired
  private GameService gameService;

  @Autowired
  private UsersService usersService;

  @RequestMapping("/allMages")
  public List<MageDTO> allMages() {
    return EntryStream.of(gameService.getMages())
        .map(MageDTO::new)
        .map(dto -> dto.withPasswordHash(usersService.getUser(dto.getName()).getPassword()))
        .toList();
  }

  public static class BuildingDTO {
    private String name;
    private int level;
    private int number;

    public BuildingDTO(MageBuildings mageBuildings) {
      this.name = mageBuildings.getName();
      this.level = mageBuildings.getLevel();
      this.number = mageBuildings.getNumber();
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getLevel() {
      return level;
    }

    public void setLevel(int level) {
      this.level = level;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

  }

  public static class MageDTO {
    private String name;
    private String passwordHash;
    private List<BuildingDTO> buildings;
    private double mana;
    private long lastManaUpdate;
    private int fightWins;

    public MageDTO(Map.Entry<String, Mage> mageEntry) {
      this.name = mageEntry.getKey();
      Mage mage = mageEntry.getValue();
      this.buildings = StreamEx.of(mage.getBuildings()).map(BuildingDTO::new).toList();
      this.mana = mage.getMana();
      this.lastManaUpdate = mage.getLastManaUpdate();
      this.fightWins = mage.getFightWins();
    }

    public MageDTO withPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
      return this;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPasswordHash() {
      return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
    }

    public List<BuildingDTO> getBuildings() {
      return buildings;
    }

    public void setBuildings(List<BuildingDTO> buildings) {
      this.buildings = buildings;
    }

    public double getMana() {
      return mana;
    }

    public void setMana(double mana) {
      this.mana = mana;
    }

    public long getLastManaUpdate() {
      return lastManaUpdate;
    }

    public void setLastManaUpdate(long lastManaUpdate) {
      this.lastManaUpdate = lastManaUpdate;
    }

    public int getFightWins() {
      return fightWins;
    }

    public void setFightWins(int fightWins) {
      this.fightWins = fightWins;
    }
  }
}
