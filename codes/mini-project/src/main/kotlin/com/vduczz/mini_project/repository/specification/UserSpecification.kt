package com.vduczz.mini_project.repository.specification

import com.vduczz.mini_project.model.User
import com.vduczz.mini_project.model.UserStatus
import org.springframework.data.jpa.domain.Specification

// sử dụng object -> static methods/
object UserSpecification {

    // keyword
    fun hasKeyword(keyword: String?): Specification<User>? {
        if (keyword.isNullOrBlank()) return null // không truyền -> skip

        // Specification {root (or entity), query, builder}
        return Specification { entity, _, builder ->
            // pattern
            val pattern = builder.literal("%${keyword.lowercase()}%")

            // fullname = "$firstName $lastName"
            //            val fullName = builder.function(
            //                "concat_ws",
            //                String::class.java,
            //                builder.literal(" "),
            //                entity.get<String>("firstName"),
            //                entity.get<String>("lastName")
            //            )
            val fullName = builder.concat(
                builder.concat(entity.get<String>("firstName"), " "),
                entity.get<String>("lastName")
            )

            builder.or(
                // username query
                builder.like(builder.lower(entity.get("username")), pattern),
                // fullname query
                builder.like(builder.lower(fullName), pattern),
                // email query
                builder.like(builder.lower(entity.get("email")), pattern)
            )
        }
    }

    fun hasStatus(isActive: Boolean?): Specification<User>? {
        if (isActive == null) return null

        return Specification { entity, _, builder ->
            if (isActive) builder.equal(
                entity.get<UserStatus>("status"),
                UserStatus.ACTIVED
            )
            else builder.notEqual(
                entity.get<UserStatus>("status"),
                UserStatus.ACTIVED
            )
        }
    }


}