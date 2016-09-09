package idlemage.game;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta")
public class MetaController {

	@Autowired
	private GameResources gameResources;

	@RequestMapping("/buildings")
	public Stream<String> buildings() {
		return gameResources.getCreaturesList().stream().map(Building::getName);
	}
}
