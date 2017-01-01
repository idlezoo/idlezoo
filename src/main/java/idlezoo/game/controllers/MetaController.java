package idlezoo.game.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlezoo.game.domain.Building;
import idlezoo.game.services.ResourcesService;
import one.util.streamex.StreamEx;

@RestController
@RequestMapping("/meta")
public class MetaController {

  @Autowired
  private ResourcesService gameResources;

  @GetMapping("/buildings")
  public List<String> buildings() {
    return StreamEx.of(gameResources.getAnimalsList())
        .map(Building::getName).toList();
  }
}
