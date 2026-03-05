package com.vduczz.mini_project.service

import com.vduczz.mini_project.model.User

interface NotificationService {
    fun notify(toUser: User, subject: String, payload: String)
}