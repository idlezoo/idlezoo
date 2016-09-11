package idlemage.game.controllers;

import static java.util.Comparator.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Mage;
import idlemage.game.domain.MageBuildings;
import idlemage.game.services.GameService;
import one.util.streamex.EntryStream;

@RestController
@RequestMapping("/top")
public class TopController {

  @Autowired
  private GameService gameService;

  @RequestMapping("/building/{building}")
  public List<TopEntry<Integer>> building(@PathVariable String building) {
    return EntryStream.of(gameService.getMages())
        .mapValues(mage -> mage.getBuildingsMap().get(building))
        .filterValues(Objects::nonNull)
        .mapValues(MageBuildings::getNumber)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::new)
        .toList();
  }

  @RequestMapping("/income")
  public List<TopEntry<Double>> income() {
    return EntryStream.of(gameService.getMages())
        .mapValues(Mage::getIncome)
        .reverseSorted(comparingDouble(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::new)
        .toList();
  }
  
  @RequestMapping("/wins")
  public List<TopEntry<Integer>> wins() {
    return EntryStream.of(gameService.getMages())
        .mapValues(Mage::getFightWins)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::new)
        .toList();
  }
  

  public static class TopEntry<V> {
    private final String name;
    private final V value;

    public TopEntry(Map.Entry<String, V> entry) {
      this.name = entry.getKey();
      this.value = entry.getValue();
    }

    public TopEntry(String name, V value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public V getValue() {
      return value;
    }
  }

}
