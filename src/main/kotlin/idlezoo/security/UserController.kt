package idlezoo.security

import java.security.Principal

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping("/user")
    fun user(user: Principal?): Principal? = user

}
