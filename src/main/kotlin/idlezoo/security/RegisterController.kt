package idlezoo.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RegisterController(private val usersService: UsersService) {

    @PostMapping("/createuser")
    fun user(@RequestParam username: String, @RequestParam password: String): ResponseEntity<String> {
        return if (usersService.addUser(username, password)) {
            ResponseEntity(HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }
}
