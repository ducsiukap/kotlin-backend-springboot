package com.vduczz.mini_project.repository.specification

import org.springframework.data.jpa.domain.Specification

fun <T : Any> nullSpecification(): Specification<T> =
    Specification { _, _, builder -> builder.conjunction() }


fun <T : Any> List<Specification<T>?>.toSpecification(): Specification<T> {
    return this.filterNotNull()
        .reduceOrNull { acc, spec ->
            acc.and(spec)
        } ?: nullSpecification()
}