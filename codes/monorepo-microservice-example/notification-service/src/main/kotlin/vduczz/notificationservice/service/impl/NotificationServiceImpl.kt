package vduczz.notificationservice.service.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import vduczz.notificationservice.controller.dto.WelcomeMailRequest
import vduczz.notificationservice.service.NotificationService

@Service
class NotificationServiceImpl(
    private val mailSender: MailSender,

    @Value("\${spring.mail.username}")
    private val serverMail: String
) : NotificationService {

    // chạy async
    @Async("taskExecutor")
    override fun sendWelcomeMail(request: WelcomeMailRequest) {
        try {
            val message = SimpleMailMessage();

            message.from = serverMail
            message.setTo(request.email)

            message.subject = "Welcome ${request.name}!!!"
            message.text = "Hello World!"

            mailSender.send(message)

        } catch (ex: Exception) {
            // log
            println("failed: ${ex.message}")
        }
    }
}