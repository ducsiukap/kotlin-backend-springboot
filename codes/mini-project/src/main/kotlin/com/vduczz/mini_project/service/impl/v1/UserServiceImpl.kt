package com.vduczz.mini_project.service.impl.v1

import com.vduczz.mini_project.core.command.CreateUserCommand
import com.vduczz.mini_project.core.command.UpdateUserCommand
import com.vduczz.mini_project.core.command.UserFilterCommand
import com.vduczz.mini_project.core.command.toEntity
import com.vduczz.mini_project.core.command.updateEntity
import com.vduczz.mini_project.core.exception.DuplicateUsernameException
import com.vduczz.mini_project.core.exception.InvalidCredentialsException
import com.vduczz.mini_project.core.exception.UserNotFoundException
import com.vduczz.mini_project.dto.request.UserFilter
import com.vduczz.mini_project.dto.response.PageResponse
import com.vduczz.mini_project.model.User
import com.vduczz.mini_project.repository.UserRepository
import com.vduczz.mini_project.repository.specification.UserSpecification
import com.vduczz.mini_project.repository.specification.toSpecification
import com.vduczz.mini_project.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ReflectionUtils
import java.util.UUID

@Service // @Service Annotation : Service class -> Bean
class UserServiceImpl(
    // DI -> Repository
    // @Autowired // @Autowired, optional
    val userRepository: UserRepository
) : UserService {

    // ------------------------------------------------------------
    // CRUD basic

    // ------------------------------------------------------------
    // Manual pagination
    // search user by fullName
    @Transactional(readOnly = true)
    override fun getListUsers(
        keyword: String?,
        page: Int,
        size: Int,
    ): PageResponse<User> {

        // Sort
        val sort = Sort
            .by(Sort.Order.asc("lastName"))
            .and(Sort.by("firstName"))
            .and(Sort.by("email"))
            .and(Sort.by("createdAt").descending())

        // Pageable(pageOrder, pageSize)
        // Pageable(pageOrder, pageSize, sort)
        val pageable = PageRequest.of(page, size, sort)

        // data
        val data =
            if (keyword.isNullOrBlank()) userRepository.findAll(pageable)
            else userRepository.findByFullname(keyword = keyword, pageable = pageable)

        // trả về Page<User>
        return PageResponse(
            data = data.content, // List<User>
            page = data.number, // page number
            size = data.size, // page size
            totalElements = data.totalElements,
            totalPages = data.totalPages
        )
    }

    // ------------------------------------------------------------
    // Dynamic Filtering & Pagination
    @Transactional(readOnly = true)
    override fun getListUsers(
        filter: UserFilterCommand
    ): PageResponse<User> {

        // specification
        // val spec = Specification.where<User>(null) // cay vl đéo nhận null
        val spec = listOf(
            UserSpecification.hasKeyword(keyword = filter.keyword),
            UserSpecification.hasStatus(isActive = filter.isActive)
        ).toSpecification()

        // take pageable
        val pageable = filter.pageable

        // data
        val data = userRepository.findAll(spec, pageable)
        // Page<T> findAll(Specification<T> spec, Pageable pageable);
        // implements JpaSpecification -> các hàm đã có sẵn tự có pageable
        // implements JpaSpecificationExecutor -> mỗi hàm tự có thêm specification

        // trả về Page<User>
        return PageResponse(
            data = data.content, // List<User>
            page = data.number, // page number
            size = data.size, // page size
            totalElements = data.totalElements,
            totalPages = data.totalPages
        )
    }

    // Read
    @Transactional(readOnly = true)
    override fun getDetailUser(id: UUID): User {
        // Nếu không thấy -> throw exception
        // không nên return null
        return userRepository.findById(id).orElseThrow { // elseOrThrow
            // use custom exception
            // có thể dùng RuntimeException nhưng nên tránh
            throw UserNotFoundException(value = id)
        }
    }

    // login
    @Transactional(readOnly = true)
    override fun checkLogin(username: String, password: String): User {

        val user = userRepository.findFirstByUsernameAndPassword(username, password)

        if (user == null) {
            throw InvalidCredentialsException()
        } else {
            return user
        }
    }

    // C/U/D -> update
    // C
    @Transactional(rollbackFor = [Exception::class]) // rollback for any-exception
    override fun createUser(user: CreateUserCommand): User {

        if (userRepository.isExistedUser(user.username)) {
            throw DuplicateUsernameException(username = user.username)
        }

        return userRepository.save(user.toEntity())
    }

    // D
    @Transactional(rollbackFor = [Exception::class])
    override fun deleteUser(id: UUID) {

        //
        val deletedUser = userRepository.findById(id).orElseThrow {
            throw UserNotFoundException(value = id)
        }

        userRepository.delete(deletedUser)
    }

    // U
    @Transactional(rollbackFor = [Exception::class])
    override fun updateUser(id: UUID, user: UpdateUserCommand): User {
        val updatedUser = getDetailUser(id)

        user.updateEntity(updatedUser)

        return userRepository.save(updatedUser)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateUserPartial(id: UUID, updates: Map<String, Any>): User {
        val updatedUser = getDetailUser(id)

        updates.forEach { (key, value) ->
            val field = ReflectionUtils.findField(User::class.java, key)

            if (field != null) {

                field.isAccessible = true // unlock field

                ReflectionUtils.setField(field, updatedUser, value)
            }
        }

        return userRepository.save(updatedUser)
    }

}