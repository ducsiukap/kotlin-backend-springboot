package com.vduczz.mini_project.core.command

import org.springframework.data.domain.Pageable

data class UserFilterCommand(
    val keyword: String?,
    val isActive: Boolean?,
    val pageable: Pageable
)