package vduczz.notificationservice.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.service.NotificationService

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val service: NotificationService
) {

    @PostMapping("/welcome")
    fun sendWelcomeMail(
        @RequestBody request: WelcomeMailRequest
    ): ResponseEntity<Unit> {
        service.sendWelcomeMail(request);

        return ResponseEntity.ok().build()
    }
}