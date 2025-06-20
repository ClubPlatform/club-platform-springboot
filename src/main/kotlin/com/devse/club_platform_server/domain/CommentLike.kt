package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
댓글 좋아요 정보를 관리하는 엔티티
- 사용자의 댓글 좋아요 기록 저장
- 중복 좋아요 방지를 위한 복합 유니크 제약 조건
 */

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