package com.vduczz.ksb_demo.repository.demo

import com.vduczz.ksb_demo.model.demo.PostDemo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostRepositoryDemo : JpaRepository<PostDemo, UUID> {

    // 1. Property Traversal : truy vấn props của relatives mà không cần JOIN :P
    // "dấu chấm ngầm" -> viết hóa chữ cái để đi từ bảng này sang bảng kia
    // Entity+Field -> reference tới Field của Entity
    // findByUserEmail = findBY + User + Email -> findBy + User.email
    fun findByUserEmail(email: String): List<PostDemo>
    // notes: nếu post có field: userEmail -> sử dụng User_Email để phân biệt User.email với userEmail của post

    // 2. @Query -> JOIN => rõ ràng + dynamic

    // 2.1. implicit JOIN -> sử dụng . -> tự động dịch thành INNER JOIN
    @Query(
        """
        SELECT p 
        FROM PostDemo p
        WHERE LOWER(p.user.fullName) LIKE CONCAT('%', :keyword, '%')
            OR LOWER(p.title) LIKE CONCAT('%', :keyword, '%')
            OR LOWER(p.content) LIKE CONCAT('%', :keyword, '%')
    """
    ) // p.user -> tự động INNER JOIN giữa P và user
    fun findByRelatedKeyword(@Param("keyword") keyword: String): List<PostDemo>

    // 2.2. explicit JOIN
    @Query(
        """
        SELECT DISTINCT p 
        FROM PostDemo p
        JOIN p.user u
        WHERE u.id = :userId
            AND u.status = 'ACTIVE'
    """
    )
    fun findByUserId(userId: String): List<PostDemo>

    // LEFT JOIN

    // JOIN on conditions
    //      JOIN p.user u ON u.status='ACTIVE'
}