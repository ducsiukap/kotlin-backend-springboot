package com.vduczz.ksb_demo.repository.demo

import com.vduczz.ksb_demo.dto.demo.UserSummaryDemo
import com.vduczz.ksb_demo.model.demo.UserDemo
import com.vduczz.ksb_demo.model.demo.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository // optional (implicit) but highly recommended to use @Repository
interface UserRepositoryDemo : JpaRepository<UserDemo, UUID> {
    // - là INTERFACE, không phải CLASS
    // - implements JpaRepository<T, ID> with:
    //      - T is models's class -> Entity
    //      - ID is @Id column's class

    // ==================== Dynamic Proxy ====================
    /* - Repository là tuy có thể là NO-BODY INTERFACE
       - Spring sử dụng Dynamic Proxy: sinh code lúc lúc Runtime
            => Spring tự động tạo Java class implements Repository interface này,
            thực hiện thao tác với EntityManager và biến nó thành @Bean để gọi ở Service
    */// =======================================================


    // Query Method Derivation - kỹ thuật đặt tên method
    // -> Spring có khả năng dịch tên hàm ra câu lệnh SQL :)

    // 1. Tìm chính xác
    // SELECT * FROM users
    // fun findAll() (có sẵn)
    // SELECT * FROM users WHERE email = ?
    fun findByEmail(email: String): UserDemo?

    // 2. AND / OR
    fun findByStatusAndAgeGreaterThan(status: UserStatus, age: Int): List<UserDemo>

    // 3. LIKE %...%
    fun findByFullNameContainingIgnoreCase(keyword: String): List<UserDemo>

    // 4. EXIST =>  SELECT 1 FROM users WHERE email=? LIMIT 1
    fun existsByEmail(email: String): Boolean

    // 5. COUNT
    fun countByStatus(status: UserStatus): Long

    // 6. DELETE
    fun deleteByEmail(email: String)


    // --------------------------------------------------------------
    // khi SQL command phức tạp => sử dụng @Query để viết truy vấn
    // => @Query(JPQL)
    // => @Query(value=SQLCommand, nativeCode=true)

    // Select
    @Query(
        """
        SELECT u 
        FROM UserDemo u
        WHERE LOWER(u.email) = LOWER(:email)
            AND u.status = :status
    """
    )
    fun findUserByFullnameAndStatus(
        fullname: String,  // không cần @Param("email") nếu tên param của method match với tên param trong query
        status: UserStatus
    ): List<UserDemo>

    // với UPDATE / DELETE -> bắt buộc dùng @Modifying7
    @Modifying
    @Query(
        """
        UPDATE UserDemo u
        SET u.status = 'BANNED' 
        WHERE u.id = :id
    """
    )
    fun banUser(@Param("id") userId: UUID): Int // số row bị ảnh hưởng

    // ---------------------------------
    // JPQL không có LIMIT, sử dụng:
    //  + `First` trong method name, hoặc
    //  + PageRequest.of(0, 1) // or of(0, n) cho LIMIT n
    fun findFirstByEmail(email: String): UserDemo?

    // ---------------------------------
    // Pagination - phân trang
    // thêm pageable -> tự động thêm LIMIT, OFFSET và tự sinh lệnh đếm (COUNT(*))
    fun findByStatus(
        status: UserStatus,
        pageable: Pageable // thêm pageable
    ): Page<UserDemo> // return Page<T>

    @Query(
        """
        SELECT u 
        FROM UserDemo u
        WHERE LOWER(u.email) LIKE LOWER(:keyword)
    """
    )
    fun findByEmailContainingIgnoreCase(
        keyword: String,
        pageable: Pageable
    ): Page<UserDemo>
    // khi này, ở service truyền vào biến pageable:
    // PageRequest.of(page, limit, sort) // sort is optional


    // ---------------------------------
    // Interface Projection / dto -> lấy các cột cần lấy
    fun findByEmailContainingIgnoreCase(
        keyword: String
    ): List<UserSummaryDemo> // trả về UserSummary -> tự động lấy các Column tương ứng với field


    // ===================================
    // N+1 Query problem solutions

    // 1. JOIN FETCH
    // -> sử dụng JPQL ép Hibernate JOIN FETCH / LEFT JOIN FETCH ngay từ đầu
    @Query(
        """
        SELECT u 
        FROM UserDemo u
        LEFT JOIN FETCH u.posts
        WHERE u.status = 'ACTIVE'
    """
    )
    fun findUserAndPosts(): List<UserDemo>

    // 2. EntityGraph
    // -> y hệt JOIN FETCH nhưng code sạch hơn :)
    @EntityGraph(attributePaths = ["posts"])
    fun findByStatus(status: UserStatus): List<UserDemo>

    // 3. @BatchSize
    // > JOIN FETCH không dùng được với pagination, cụ thể hơn là quan hệ 1-N nếu có Pageable
    // -> sử dụng batch-size
    @Query("SELECT u FROM UserDemo u WHERE u.status = :status") // Không JOIN-FETCH
    fun getUser(@Param("status") status: UserStatus, pageable: Pageable): Page<UserDemo>
    // khi này, posts trong UserDemo vẫn là proxy,
    // nhưng khi động tới 1 cái proxy, -> nó sẽ lấy cho min(pageSize, BatchSize) cái proxy khác chưa load
    // SELECT * FROM posts WHERE user_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // min(pageSize, batchSize)=10
    // sau đó fill data cho các proxy đó => chỉ tốn 2 hoặc pageSize/batchsize+1 query thay vì N+1
}