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
	public MageDTO me(Principal user) {
		return new MageDTO(gameService.getMage(user.getName()).updateMana());
	}
	
	@RequestMapping("/me/mana")
	public double mana(Principal user) {
		return me(user).getMana();
	}

	@RequestMapping("/buy")
	public MageDTO buy(Principal user, String building) {
		return new MageDTO(gameService.getMage(user.getName()).buy(building));
	}

	@RequestMapping("/upgrade")
	public MageDTO upgrade(Principal user, String building) {
		return new MageDTO(gameService.getMage(user.getName()).upgrade(building));
	}

}
