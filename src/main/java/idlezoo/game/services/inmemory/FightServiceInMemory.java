package idlezoo.game.services.inmemory;

import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;

@Service
@Profile("default")
public class FightServiceInMemory implements FightService {

	private final Storage storage;

	private Integer waitingFighter;

	public FightServiceInMemory(Storage storage) {
		super();
		this.storage = storage;
	}

	@Override
	public synchronized Zoo fight(Integer id) {
		if (Objects.equals(id, waitingFighter)) {
			return null;
		}

		if (waitingFighter == null) {
			waitingFighter = id;
			storage.getZoo(waitingFighter).startWaitingForFight();
			return null;
		}
		InMemoryZoo waiting = storage.getZoo(waitingFighter);
		InMemoryZoo fighter = storage.getZoo(id);

		waiting.fight(fighter);
		waiting.endWaitingForFight();
		waitingFighter = null;
		return waiting.updateMoney().toDTO();
	}

}
