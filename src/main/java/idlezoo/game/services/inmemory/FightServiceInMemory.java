package idlezoo.game.services.inmemory;

import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.services.FightService;
import idlezoo.game.services.ResourcesService;

@Service
@Profile("default")
public class FightServiceInMemory implements FightService {

	private final Storage storage;
	private final ResourcesService resourcesService;

	public FightServiceInMemory(Storage storage, ResourcesService resourcesService) {
		this.storage = storage;
		this.resourcesService=resourcesService;
	}

	@Override
	public synchronized OutcomeContainer fight(Integer id) {
		if (Objects.equals(id, storage.getWaitingFighter())) {
			return OutcomeContainer.WAITING;
		}

		if (storage.getWaitingFighter() == null) {
			storage.setWaitingFighter(id);
			storage.getZoo(id).startWaitingForFight();
			return OutcomeContainer.WAITING;
		}
		InMemoryZoo waiting = storage.getZoo(storage.getWaitingFighter());
		InMemoryZoo fighter = storage.getZoo(id);

		Outcome outcome = waiting.fight(fighter);
		waiting.endWaitingForFight();
		storage.setWaitingFighter(null);
		return new OutcomeContainer(outcome, waiting.updateMoney().toDTO(resourcesService));
	}

}
