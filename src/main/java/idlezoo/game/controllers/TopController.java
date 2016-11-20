package idlezoo.game.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlezoo.game.domain.TopEntry;
import idlezoo.game.services.TopService;

@RestController
@RequestMapping("/top")
public class TopController {

  @Autowired
  private TopService topService;

  @RequestMapping("/building/{building}")
  public List<TopEntry<Integer>> building(@PathVariable String building) {
    return topService.building(building);
  }

  @RequestMapping("/income")
  public List<TopEntry<Double>> income() {
    return topService.income();
  }

  @RequestMapping("/wins")
  public List<TopEntry<Integer>> wins() {
    return topService.wins();
  }
  
  @RequestMapping("/losses")
  public List<TopEntry<Integer>> losses() {
    return topService.losses();
  }

  @RequestMapping("/championTime")
  public List<TopEntry<Long>> championTime() {
    return topService.championTime();
  }

}
