package idlezoo.game.services;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.Zoo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class GameServiceTest extends AbstractServiceTest {

    @Autowired
    private GameService gameService;


    @Test
    void testGetZoo() {
        Zoo zoo1 = gameService.getZoo(zoo1Id);
        assertEquals(0, zoo1.getFightWins());
        assertEquals(1, zoo1.getBuildings().size());
        assertEquals(0, zoo1.getBuildings().get(0).getNumber());
        assertFalse(zoo1.isWaitingForFight());
        assertEquals(0, zoo1.getMoneyIncome(), 0.0001);
        assertEquals(50, zoo1.getMoney(), 0.0001);
    }

    @Test
    void testBuy() {
        String animalType = resourcesService.firstName();
        Zoo zoo1 = gameService.buy(zoo1Id, animalType);
        Building type = resourcesService.type(animalType);
        double moneyAfterBuy = resourcesService.startingMoney() - type.buildCost(0);
        assertEquals(moneyAfterBuy, zoo1.getMoney(), 0.0001);
        assertEquals(2, zoo1.getBuildings().size());
        assertEquals(1, zoo1.getBuildings().get(0).getNumber());
        assertEquals(0, zoo1.getBuildings().get(1).getNumber());
        assertEquals(type.income(0), zoo1.getMoneyIncome(), 0.0001);
    }

    @Test
    void testUpgrade() {
        setMoney(zoo1Id, 100);

        String animalType = resourcesService.firstName();
        Zoo zoo1 = gameService.upgrade(zoo1Id, animalType);
        Building type = resourcesService.type(animalType);
        double moneyAfterUpgrade = 100 - type.upgradeCost(0);
        assertEquals(moneyAfterUpgrade, zoo1.getMoney(), 0.0001);
        assertEquals(1, zoo1.getBuildings().size());
        assertEquals(1, zoo1.getBuildings().get(0).getLevel());
    }

    @Test
    void testBuyPerk() {
        setMoney(zoo1Id, 1000_000_000);
        String animal = resourcesService.firstName();
        Zoo zoo1 = null;
        for (int i = 0; i < 100; i++) {
            zoo1 = gameService.buy(zoo1Id, animal);
        }
        assertEquals(100, zoo1.getBuildings().get(0).getNumber());
        assertEquals(0, zoo1.getPerks().size());
        assertEquals(3, zoo1.getAvailablePerks().size());
        assertEquals(100, zoo1.getBaseIncome(), 0.0001);
        assertEquals(100, zoo1.getMoneyIncome(), 0.0001);

        zoo1 = gameService.buyPerk(zoo1Id, resourcesService.getPerkList().get(0).getName());
        assertEquals(100, zoo1.getBuildings().get(0).getNumber());
        assertEquals(1, zoo1.getPerks().size());
        assertEquals(100, zoo1.getBaseIncome(), 0.0001);
        assertEquals(150, zoo1.getMoneyIncome(), 0.0001);
        assertEquals(2, zoo1.getAvailablePerks().size());
    }
}
