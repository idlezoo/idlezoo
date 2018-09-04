package idlezoo.game

import idlezoo.game.services.TopService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
class RootRedirectController {

    @GetMapping("/")
    fun index(): String {
        return "redirect:https://idlezoo.github.io"
    }
}

@RestController
@RequestMapping("/top")
class TopController(private val topService: TopService) {
    @GetMapping("/building/{building}")
    fun building(@PathVariable building: String) = topService.building(building)

    @GetMapping("/income")
    fun income() = topService.income()

    @GetMapping("/wins")
    fun wins() = topService.wins()

    @GetMapping("/losses")
    fun losses() = topService.losses()

    @GetMapping("/championTime")
    fun championTime() = topService.championTime()
}
