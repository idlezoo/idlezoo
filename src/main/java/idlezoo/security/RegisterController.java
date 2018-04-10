package idlezoo.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

	private final UsersService usersService;

	public RegisterController(UsersService usersService) {
		this.usersService = usersService;
	}

	@PostMapping("/createuser")
	public ResponseEntity<String> user(@RequestParam String username, @RequestParam String password) {
		if (usersService.addUser(username, password)) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}
}
