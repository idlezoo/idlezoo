package idlemage.game.controllers;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import idlemage.game.domain.Building;
import idlemage.game.services.ResourcesService;

@RestController
@RequestMapping("/meta")
public class MetaController {

	@Autowired
	private ResourcesService gameResources;

	@RequestMapping("/buildings")
	public Stream<String> buildings() {
		return gameResources.getCreaturesList().stream().map(Building::getName);
	}
}
