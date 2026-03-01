package com.vduczz.mini_project.repository

import com.vduczz.mini_project.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    // JpaSpecificationExecutor<T> -> cho phép dynamic filtering

    // check login
    fun findFirstByUsernameAndPassword(username: String, password: String): User?

    // is valid new user
    @Query(
        """
        SELECT COUNT(u) > 0
        FROM User u
        WHERE u.username = :username
    """
    )
    fun isExistedUser(@Param("username") username: String): Boolean

    // ------------------------------------------------------------
    // Pagination
    // => thêm Pageable làm tham số của function

    // Note: Pagination với những hàm có @Query
    // - với JPQL : Spring tự sinh ra lệnh đếm
    // - với Native SQL: cần phải thêm lệnh đếm
    @Query(
        """
        SELECT u FROM User u
        WHERE CONCAT(u.firstName,' ', u.lastName) LIKE CONCAT('%', :keyword, '%')
    """
    )
    //    @Query(
    //        """
    //        SELECT * FROM users
    //        WHERE CONCAT_WS(' ', u.first_name, u.last_name)
    //                LIKE CONCAT('%', :keyword, '%')
    //        """, // CONCAT_WS(sep, args) : concat with separator
    //
    //        nativeQuery = true,
    //
    //        // nativeQuery -> cần sinh lênh count
    //        countQuery = """
    //        SELECT COUNT(*) FROM users
    //        WHERE CONCAT_WS(' ', u.first_name, u.last_name)
    //                LIKE CONCAT('%', :keyword, '%')
    //        """
    //    )
    fun findByFullname(
        @Param("keyword")
        keyword: String,
        pageable: Pageable // enable pageable
    ): Page<User> // trả về Page
}