package idlezoo.game.controllers;

import static java.util.Comparator.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlezoo.game.domain.Zoo;
import idlezoo.game.domain.ZooBuildings;
import idlezoo.game.services.GameService;
import one.util.streamex.EntryStream;

@RestController
@RequestMapping("/top")
public class TopController {

  @Autowired
  private GameService gameService;

  @RequestMapping("/building/{building}")
  public List<TopEntry<Integer>> building(@PathVariable String building) {
    return EntryStream.of(gameService.getZoos())
        .mapValues(zoo -> zoo.getBuildingsMap().get(building))
        .filterValues(Objects::nonNull)
        .mapValues(ZooBuildings::getNumber)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @RequestMapping("/income")
  public List<TopEntry<Double>> income() {
    return EntryStream.of(gameService.getZoos())
        .mapValues(Zoo::getIncome)
        .reverseSorted(comparingDouble(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @RequestMapping("/wins")
  public List<TopEntry<Integer>> wins() {
    return EntryStream.of(gameService.getZoos())
        .mapValues(Zoo::getFightWins)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @RequestMapping("/championTime")
  public List<TopEntry<Long>> championTime() {
    return EntryStream.of(gameService.getZoos())
        .mapValues(Zoo::getChampionTime)
        .reverseSorted(comparingLong(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  public static class TopEntry<V> {
    private final String name;
    private final V value;

    static <V> TopEntry<V> of(Map.Entry<String, V> entry){
      return new TopEntry<>(entry);
    }

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
