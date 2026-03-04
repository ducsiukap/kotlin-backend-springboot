package com.vduczz.mini_project.core.security.checker

import com.vduczz.mini_project.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component("userSecurity")
class UserSecurityChecker(
    private val userRepo: UserRepository,
) {

    // check target user in request is current user
    fun isCurrentUser(targetUID: UUID, currentUsername: String): Boolean {
        val targetUser = userRepo.findById(targetUID).orElse(null)
            ?: return false
        return targetUser.username == currentUsername
    }

}
