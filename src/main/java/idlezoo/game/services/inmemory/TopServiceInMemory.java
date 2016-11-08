package idlezoo.game.services.inmemory;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.comparingLong;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.TopEntry;
import idlezoo.game.services.TopService;
import one.util.streamex.EntryStream;

@Service
@Profile("default")
public class TopServiceInMemory implements TopService {

  private final Storage storage;

  public TopServiceInMemory(Storage storage) {
    this.storage = storage;
  }


  @Override
  public List<TopEntry<Integer>> building(String building) {
    return EntryStream.of(storage.getZoos())
        .mapValues(zoo -> zoo.getBuildingsMap().get(building))
        .filterValues(Objects::nonNull)
        .mapValues(InMemoryZooBuildings::getNumber)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @Override
  public List<TopEntry<Double>> income() {
    return EntryStream.of(storage.getZoos())
        .mapValues(InMemoryZoo::getIncome)
        .reverseSorted(comparingDouble(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @Override
  public List<TopEntry<Integer>> wins() {
    return EntryStream.of(storage.getZoos())
        .mapValues(InMemoryZoo::getFightWins)
        .reverseSorted(comparingInt(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }

  @Override
  public List<TopEntry<Long>> championTime() {
    return EntryStream.of(storage.getZoos())
        .mapValues(InMemoryZoo::getChampionTime)
        .reverseSorted(comparingLong(Map.Entry::getValue))
        .limit(10)
        .map(TopEntry::of)
        .toList();
  }
}
