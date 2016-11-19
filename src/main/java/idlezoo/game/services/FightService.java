package idlezoo.game.services;

import idlezoo.game.domain.Zoo;

public interface FightService {

	OutcomeContainer fight(Integer id);

	public static class OutcomeContainer {
		private final Outcome outcome;
		private final Zoo waitingFighter;

		public OutcomeContainer(Outcome outcome, Zoo waitingFighter) {
			this.outcome = outcome;
			this.waitingFighter = waitingFighter;
		}

		public static final OutcomeContainer WAITING = new OutcomeContainer(Outcome.WAITING, null);

		public static OutcomeContainer win(Zoo waitingFighter) {
			return new OutcomeContainer(Outcome.WIN, waitingFighter);
		}

		public static OutcomeContainer loss(Zoo waitingFighter) {
			return new OutcomeContainer(Outcome.LOSS, waitingFighter);
		}

		public Outcome getOutcome() {
			return outcome;
		}

		public Zoo getWaitingFighter() {
			return waitingFighter;
		}
	}

	public enum Outcome {
		WIN, LOSS, WAITING;
	}

}
