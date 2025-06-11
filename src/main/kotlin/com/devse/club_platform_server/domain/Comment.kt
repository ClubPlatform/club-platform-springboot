package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "comment")
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val commentId: Long = 0L,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "author_id", nullable = false)
    val authorId: Long,

    @Column(name = "parent_id")
    val parentId: Long? = null,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "is_anonymous", nullable = false)
    val isAnonymous: Boolean = false
)