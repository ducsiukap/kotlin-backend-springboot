package com.vduczz.mini_project.service.impl.v1

import com.vduczz.mini_project.model.User
import com.vduczz.mini_project.service.NotificationService
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class MailNotificationServiceImpl(
    // mailSender
    private val mailSender: JavaMailSender,

    // server email -> your email
    @Value("\${spring.mail.username}")
    private val serverMail: String
) : NotificationService {

    @Async("taskExecutor") // enable asynchronous process
    // thực chất: @Async tạo Proxy (class giả) bọc ngoài MailNotificationServiceImpl
    //  => khi gọi hàm -> gọi Proxy thay vì chạy code thật
    //      => Proxy đóng gói thành Task và đưa vào Thread Pool xử lý
    override fun notify(toUser: User, subject: String, payload: String) {

        val currentThread = Thread.currentThread().name

        // log
        println("[${currentThread}] Connecting to SMTP server...")

        try {

            // init simple mail message
            val message = SimpleMailMessage()

            // message metadata
            message.from = serverMail
            message.setTo(toUser.email)
            // message data
            message.subject = subject
            message.text = payload

            // send mail
            mailSender.send(message)

            println("[$currentThread] Successfully sent mail to ${toUser.email}")
        } catch (e: Exception) {
            println("[$currentThread] Exception while connecting to SMTP server: ${e.message}")
        }
    }
}