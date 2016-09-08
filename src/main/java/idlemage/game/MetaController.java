package idlemage.game;

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta")
public class MetaController {

	@RequestMapping("/buildings")
	public Collection<String> buildings() {
		return GameResources.BUILDING_TYPES.keySet();

	}
}
