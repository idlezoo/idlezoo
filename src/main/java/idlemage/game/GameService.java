package idlemage.game;

import java.util.concurrent.ConcurrentHashMap;

public class GameService {

	private final ConcurrentHashMap<String, Mage> mages = new ConcurrentHashMap<>();

	public static class Mage {

	}

	public enum CreatureKind {
		Rat {
			@Override
			public double nextCost(int index) {
				return Math.pow(1.2, index);
			}

			@Override
			public double levelGain(int level) {
				return 2.5 * level;
			}

			@Override
			public double levelUpgrade(int level) {
				return Math.pow(5, level);
			}
		},
		Wolf {
			@Override
			public double nextCost(int index) {
				return 100 + Math.pow(2, index);
			}

			@Override
			public double levelGain(int level) {
				return 2.5 * level;
			}

			@Override
			public double levelUpgrade(int level) {
				return 100 + Math.pow(5, level);
			}
		},
		Bear {
			@Override
			public double nextCost(int index) {
				return 1000 + 10 * Math.pow(4, index);
			}

			@Override
			public double levelGain(int level) {
				return 2.5 * level;
			}

			@Override
			public double levelUpgrade(int level) {
				return 1000 + 100 * Math.pow(5, level);
			}
		},
		Dragon {
			@Override
			public double nextCost(int index) {
				return 10000 + 100 * Math.pow(5, index);
			}

			@Override
			public double levelGain(int level) {
				return 3 * level;
			}

			@Override
			public double levelUpgrade(int level) {
				return 10000 + 1000 * Math.pow(5, level);
			}
		};

		public abstract double nextCost(int index);

		public abstract double levelGain(int level);

		public abstract double levelUpgrade(int lebel);

	}

}
