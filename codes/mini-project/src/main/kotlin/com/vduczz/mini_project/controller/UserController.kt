package com.vduczz.mini_project.controller

import com.vduczz.mini_project.dto.request.CreateUserRequest
import com.vduczz.mini_project.dto.request.UpdateUserRequest
import com.vduczz.mini_project.dto.request.UserFilter
import com.vduczz.mini_project.dto.request.toCommand
import com.vduczz.mini_project.dto.response.PageResponse
import com.vduczz.mini_project.dto.response.UserDetailResponse
import com.vduczz.mini_project.dto.response.toUserDetailResponse
import com.vduczz.mini_project.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// ------------------------------------------------------------
// @Controller // SpringMVC -> servlet
@RestController // RESTful API
// -> @RestController tự thêm "/api/v1" (do config/WebConfig.kt)
// @RestController = @Controller + @ResponseBody
// ------------------------------------------------------------
@RequestMapping("/users") // "/api/v1/users"
// cổ điển:
// @RequestMapping(value=["url"], method = [RequestMethod.GET])
// hiện đại:
// @RequestMapping("url")
// ------------------------------------------------------------
@Validated // cho phép validate trên biến lẻ
// nếu chỉ valid dto  -> chỉ cần dùng @Valid ở param
// ------------------------------------------------------------
// Swagger
@Tag( // @Tag -> đầu class => nhóm API
    name = "User API",
    description = """All APIs related to User"""
)
class UserController(
    // DI
    val userService: UserService
) {
    // ------------------------------------------------------------
    // Swagger
    @Operation(
        // @Operation -> gắn lên method
        //  => API cụ thể

        summary = "List of user",
        description = "Returns a list of all users, using pagination & filtering",
    )

    // ------------------------------------------------------------
    // GET
    // ------------------------------------------------------------
    // Pagination + Filtering
    @GetMapping
    fun getListUsers(
        // ------------------------------------------------------------
        // Dynamic Filter
        //      query trong param có dạng: ?field=value&otherField=value
        //      với field, otherField trùng tên với field trong UserField
        @Valid filter: UserFilter, // pageable validation trong filter

        // Pageable mapping -> url phải có dạng:
        //      GET /users?page=0&size=10&sort=age,desc&sort=username,asc
        //          page=0, size=10
        //          sort theo age trước, sau đó sort theo username
        //  nếu không truyền các tham số, nó lấy mặc đinh:
        //          page=0, size=20
        //          sort=UNSORTED
        // có thể cấu hình default-value cho các tham số đó :)
        //        @PageableDefault(
        //            page = 0, size = 10,
        //            // sort 1 or nhiều filed cùng direction
        //            //            sort = ["lastName", "firstName", "createdAt"],
        //            //            direction = Sort.Direction.ASC
        //        )
        //        // sort nhiều field, direction khác nhau
        //        @SortDefault.SortDefaults(
        //            SortDefault(sort = ["firstName", "lastName"], direction = Sort.Direction.ASC),
        //            @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        //        )
        // pageable: Pageable,

        // ------------------------------------------------------------
        // manual pagination
        //        // filtering -> ?key=value => use @RequestParam to take
        //        // pagination -> page, size
        //        @RequestParam(defaultValue = "0") page: Int,
        //        // validation -> maximum 20 user per request
        //        @Max(20) // required using @Validation on class declaration to enable field validation
        //        @RequestParam(defaultValue = "10") size: Int,
        //        @RequestParam(required = false) keyword: String?,

    ): ResponseEntity<PageResponse<UserDetailResponse>> {


        // ------------------------------------------------------------
        // manual pagination
        //        // call service
        //        val userPage = userService.getListUsers(
        //            keyword = keyword,
        //            page = page,
        //            size = size
        //        )


        // ------------------------------------------------------------
        // Dynamic Filter + Pagination
        // truyền filter vào service
        val userPage = userService.getListUsers(filter.toCommand())


        // mapping entity -> dto
        val responseData = PageResponse(
            data = userPage.data.map { it.toUserDetailResponse() },
            page = userPage.page,
            size = userPage.size,
            totalElements = userPage.totalElements,
            totalPages = userPage.totalPages
        )

        // response
        return ResponseEntity.ok(responseData)
    }

    @Operation(
        summary = "Get an user",
        description = "Get a specific user using id",
    )
    @GetMapping("/{id}")
    fun getDetailUser(
        // có thể yêu cầu valid cho cả param từ URL
        // yêu cầu `@Validated` ở đầu class để cho phép validation cho biến lẻ
        @Pattern(regexp = "^[0-9a-fA-F]{32}$", message = "UUID-type required for {id}")
        @PathVariable id: UUID //
    ): ResponseEntity<UserDetailResponse> {

        val user = userService.getDetailUser(id).toUserDetailResponse()

        return ResponseEntity.ok(user)
    }

    @Operation(
        summary = "Create a new User",
    )
    // ------------------------------------------------------------
    // POST
    @PostMapping
    fun createUser(
        @Valid // @Valid -> yêu cầu validate dto
        @RequestBody // @RequestBody -> paste JSON to dto (CreateUserRequest)
        user: CreateUserRequest
    ): ResponseEntity<UserDetailResponse> {
        val userCommand = user.toCommand()
        val userResponse = userService.createUser(userCommand).toUserDetailResponse()

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
    }

    @Operation(summary = "Update an user")
    // ------------------------------------------------------------
    // PUT
    // ------------------------------------------------------------
    // DATA-driven authorize example
    //  + use checker
    @PreAuthorize("@userSecurity.isCurrentUser(#id, authentication.name)")
    //      + authentication là context có sẵn trong Spring Security -> chứa thông tin auth
    //          + authentication.name -> username (jwt subject)
    //          + authentication.principle -> .getPrinciple() -> user (UserDetails)
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: UUID,

        // require validation
        @Valid @RequestBody user: UpdateUserRequest

    ): ResponseEntity<UserDetailResponse> {
        val userCommand = user.toCommand()

        val userResponse = userService.updateUser(id, userCommand).toUserDetailResponse()

        return ResponseEntity.ok(userResponse)
    }

    @Operation(summary = "Delete an user")
    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------
    // DATA-driven authorize example
    //  + use `@AuthenticationPrinciple
    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: UUID,

        // Inject principle (chứa validated-user)
        @AuthenticationPrincipal validatedUser: UserDetails
        // có thể cast thẳng sang entity: validatedUser: User
    ): ResponseEntity<Unit> {


        userService.deleteUser(id, validatedUser.username)

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Update partial of user")
    // ------------------------------------------------------------
    // PATCH
    @PreAuthorize("@userSecurity.isCurrentUser(#id, authentication.name)")
    @PatchMapping("/{id}")
    fun updateUserPartial(
        @PathVariable id: UUID,
        @RequestBody updates: Map<String, Unit>
    ): ResponseEntity<UserDetailResponse> {
        val updatedUser = userService.updateUserPartial(id, updates).toUserDetailResponse()

        return ResponseEntity.ok(updatedUser)
    }
}