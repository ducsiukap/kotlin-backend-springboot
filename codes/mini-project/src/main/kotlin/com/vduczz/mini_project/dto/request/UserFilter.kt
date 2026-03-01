package com.vduczz.mini_project.dto.request

import com.vduczz.mini_project.core.command.UserFilterCommand
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class UserFilter(

    // có thể gắn validation :)
    @field:Size(max = 36, message = "Too long keyword!")
    val keyword: String? = null, // name, email, ....

    // url parameter cũng nên dạng camelCase
    // ?isActive=true
    val isActive: Boolean? = null,

    // ------------------------------------------------------------
    // để tận dụng validation của filter
    // => có thể validation cho cả Pageable :)
    @field:Min(value = 0, message = "Negative page error")
    val page: Int = 0,
    @field:Min(value = 1, message = "Page size must be between 1 and 50")
    @field:Max(value = 50, message = "Page size must be between 1 and 50")
    val size: Int = 10,

    // nếu sort 1 tiêu chí
    //      val sortBy: String = "createdAt"
    //      val sortDir: String = "desc"
    // và validate trên 2 cái đó (sử dụng pattern

    // nếu sort phức tạp
    val sort: List<String> = emptyList(),
    // lúc này, url không được dùng:
    //      ?sort=age,desc&sort=username,asc
    //          (spring tách thành ["age", "desc", "username", "asc"]
    // custom url -> key:value
    //      khi này, spring giữ nguyên ["key:value", "key:value"]
) {
    fun toPageable(): Pageable {
        val sortableField = setOf("name", "email", "createdAt")
        val orders = mutableListOf<Sort.Order>()
        sort.forEach { sortItem ->
            val parts = sortItem.split(":")
            val field = parts[0]
            val dir = if (parts.size > 1) parts[1] else "asc"

            // nếu field nằm trong whitelist -> cho sort
            // ngược lại, có thể throw exception hoặc không sort tiêu chí đó
            if (sortableField.contains(field)) {
                val direction = if (dir == "asc") Sort.Direction.ASC else Sort.Direction.DESC

                if (field == "name") {
                    orders.add(Sort.Order(direction, "firstName"))
                    orders.add(Sort.Order(direction, "lastName"))
                } else
                    orders.add(Sort.Order(direction, field))
            }
        }

        // giả sử bắt buộc sort theo ngày tạo (kể cả không truyền)
        val isSortedByCreatedDate = orders.any { it.property == "createdAt" }
        if (!isSortedByCreatedDate) orders.add(Sort.Order.desc("createdAt"))

        // return pageable
        return PageRequest.of(page, size, Sort.by(orders))
    }
}

fun UserFilter.toCommand(): UserFilterCommand = UserFilterCommand(
    keyword = this.keyword,
    isActive = this.isActive,
    pageable = this.toPageable(),
)