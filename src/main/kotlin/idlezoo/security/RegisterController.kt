package idlezoo.security

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RegisterController(private val usersService: UsersService) {

    @PostMapping("/createuser")
    fun user(@RequestParam username: String, @RequestParam password: String) =
            if (usersService.addUser(username, password)) {
                HttpStatus.OK
            } else {
                HttpStatus.FORBIDDEN
            }

}
