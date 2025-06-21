package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
사용자 키워드 알림 설정을 관리하는 엔티티
- 사용자가 알림받고 싶은 키워드 등록
- 해당 키워드가 포함된 게시글 작성 시 알림 생성
 */

@Entity
@Table(name = "user_keyword")
data class UserKeyword(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    val keywordId: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "keyword", nullable = false)
    val keyword: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)