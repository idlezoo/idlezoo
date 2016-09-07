package idlemage.game;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
public class GameController {

	@Autowired
	private GameService gameService;

	@RequestMapping("/me")
	public Mage me(Principal user) {
		return gameService.getMage(user.getName()).updateMana();
	}

	@RequestMapping("/buy")
	public Mage buy(Principal user, String building) {
		return gameService.getMage(user.getName()).buy(building);
	}

	@RequestMapping("/upgrade")
	public Mage upgrade(Principal user, String building) {
		return gameService.getMage(user.getName()).upgrade(building);
	}

}
