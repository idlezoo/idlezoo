package idlemage.game;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import one.util.streamex.EntryStream;

@RestController
@RequestMapping("/top")
public class TopController {

	@Autowired
	private GameService gameService;

	@RequestMapping("/building/{building}")
	public Map<String, Integer> building(@PathVariable String building) {
		return EntryStream.of(gameService.getMages())
				.mapValues(mage -> mage.getBuildingsMap().get(building))
				.filterValues(Objects::nonNull)
				.mapValues(MageBuildings::getNumber)
				.sortedByInt(Map.Entry::getValue)
				.limit(10)
				.toMap();
	}

	@RequestMapping("/income")
	public Map<String, Double> income() {
		return EntryStream.of(gameService.getMages())
				.mapValues(Mage::getIncome)
				.sortedByDouble(Map.Entry::getValue)
				.limit(10)
				.toMap();
	}

}
