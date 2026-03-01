package com.vduczz.mini_project.dto.response

// Pagination
// PageResponse: container chứa pagination-data cho mọi data -> T
data class PageResponse<T>(
    val data: List<T>, // data
    val page: Int, // current page
    val size: Int, // page size -> number of elements in page
    val totalElements: Long, // total of elements in DB
    val totalPages: Int, // total of page (ứng với total-element và page-max-size)
)