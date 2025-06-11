package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "post")
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    val postId: Long = 0L,

    @Column(name = "board_id", nullable = false)
    val boardId: Long,

    @Column(name = "author_id", nullable = false)
    val authorId: Long,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "content", columnDefinition = "TEXT")
    val content: String? = null,

    @Column(name = "is_notice", nullable = false)
    val isNotice: Boolean? = null,

    @Column(name = "view_count", nullable = false)
    val viewCount: Int = 0,

    @Column(name = "like_count", nullable = false)
    val likeCount: Int = 0,

    @Column(name = "comment_count", nullable = false)
    val commentCount: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "is_anonymous", nullable = false)
    val isAnonymous: Boolean = false
)