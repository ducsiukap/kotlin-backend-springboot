package vduczz.notificationservice.service

import vduczz.notificationservice.controller.dto.WelcomeMailRequest

interface NotificationService {
    fun sendWelcomeMail(request: WelcomeMailRequest)
}