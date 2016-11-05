package idlezoo.game.services;

import java.util.List;

import idlezoo.game.domain.TopEntry;

public interface TopService {

  List<TopEntry<Integer>> building(String building);

  List<TopEntry<Double>> income();

  List<TopEntry<Integer>> wins();

  List<TopEntry<Long>> championTime();

}
