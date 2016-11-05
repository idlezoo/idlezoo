package idlezoo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import idlezoo.game.services.GameService;

@RestController
public class RegisterController {

	@Autowired
	private UsersService usersService;

	@Autowired
	private GameService gameService;

	@RequestMapping("/createuser")
	public ResponseEntity<String> user(@RequestParam String username, @RequestParam String password) {
		if (usersService.addUser(username, password)) {
			gameService.createZoo(username);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}
}
