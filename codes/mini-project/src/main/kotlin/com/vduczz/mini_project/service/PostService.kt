//package com.vduczz.mini_project.service
//
//import com.vduczz.mini_project.model.Post
//import com.vduczz.mini_project.repository.PostRepository
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.util.UUID
//
//@Service // @Service Annotation : Service class -> Bean
//class PostService(
//    // DI -> Repository
//    // @Autowired // @Autowired, optional
//    val postRepository: PostRepository,
//) {
//
//    // ------------------------------------------------------------
//    // CRUD basic
//
//    // Read
//    @Transactional(readOnly = true) // performance
//    fun getListPosts(): List<Post> = postRepository.findAll()
//
//    @Transactional(readOnly = true)
//    fun getDetailPost(id: UUID): Post? {
//        // Nếu không thấy -> throw exception
//        // không nên return null
//        return postRepository.findById(id).orElseThrow { // elseOrThrow
//            RuntimeException("Post not found for id: $id")
//        }
//    }
//
//    // C/U/D -> update
//    // D
//    @Transactional(rollbackFor = [Exception::class])
//    fun deletePost(id: UUID) {
//
//        // first -> lấy post
//        val post = getDetailPost(id)
//
//        // delete
//        postRepository.delete(post)
//    }
//    //
//
//}