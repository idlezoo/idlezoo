package idlemage.game.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Building;
import idlemage.game.services.ResourcesService;
import one.util.streamex.StreamEx;

@RestController
@RequestMapping("/meta")
public class MetaController {

	@Autowired
	private ResourcesService gameResources;

	@RequestMapping("/buildings")
	public List<String> buildings() {
	  return StreamEx.of(gameResources.getCreaturesList())
	  .map(Building::getName).toList();
	}
}
