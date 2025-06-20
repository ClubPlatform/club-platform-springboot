package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
댓글 정보를 관리하는 엔티티
- 게시글에 달리는 댓글 및 대댓글 저장
- 부모 댓글 참조를 통한 계층 구조 지원
- 익명 댓글 작성 기능 지원
- 댓글 작성자 및 작성 시간 추적
 */

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