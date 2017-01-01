package idlezoo.game.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("/building/{building}")
  public List<TopEntry<Integer>> building(@PathVariable String building) {
    return topService.building(building);
  }

  @GetMapping("/income")
  public List<TopEntry<Double>> income() {
    return topService.income();
  }

  @GetMapping("/wins")
  public List<TopEntry<Integer>> wins() {
    return topService.wins();
  }
  
  @GetMapping("/losses")
  public List<TopEntry<Integer>> losses() {
    return topService.losses();
  }

  @GetMapping("/championTime")
  public List<TopEntry<Long>> championTime() {
    return topService.championTime();
  }

}
