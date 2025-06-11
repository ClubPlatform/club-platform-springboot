package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "comment_like",
    uniqueConstraints = [UniqueConstraint(columnNames = ["comment_id", "user_id"])]
)
data class CommentLike(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    val likeId: Long = 0L,

    @Column(name = "comment_id", nullable = false)
    val commentId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)