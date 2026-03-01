package com.vduczz.mini_project.service

import com.vduczz.mini_project.core.command.CreateUserCommand
import com.vduczz.mini_project.core.command.UpdateUserCommand
import com.vduczz.mini_project.core.command.UserFilterCommand
import com.vduczz.mini_project.dto.request.UserFilter
import com.vduczz.mini_project.dto.response.PageResponse
import com.vduczz.mini_project.dto.response.UserDetailResponse
import com.vduczz.mini_project.model.User
import java.util.UUID

interface UserService {

    fun getDetailUser(id: UUID): User

    fun checkLogin(username: String, password: String): User

    fun getListUsers(keyword: String?, page: Int, size: Int): PageResponse<User>
    fun getListUsers(filter: UserFilterCommand): PageResponse<User>

    fun createUser(user: CreateUserCommand): User

    fun deleteUser(id: UUID)

    fun updateUser(id: UUID, user: UpdateUserCommand): User

    fun updateUserPartial(id: UUID, updates: Map<String, Any>): User

}