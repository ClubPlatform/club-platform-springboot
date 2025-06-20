package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
게시글 스크랩 정보를 관리하는 엔티티
- 사용자의 게시글 스크랩 기록 저장
- 중복 스크랩 방지를 위한 복합 유니크 제약 조건
 */

@Entity
@Table(
    name = "scrap",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "post_id"])]
)
data class Scrap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    val scrapId: Long = 0L,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "scrapped_at", nullable = false)
    val scrappedAt: LocalDateTime = LocalDateTime.now()
)