package idlezoo.game.services.inmemory;

import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.services.FightService;

@Service
@Profile("default")
public class FightServiceInMemory implements FightService {

	private final Storage storage;

	public FightServiceInMemory(Storage storage) {
		super();
		this.storage = storage;
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
		return new OutcomeContainer(outcome, waiting.updateMoney().toDTO());
	}

}
