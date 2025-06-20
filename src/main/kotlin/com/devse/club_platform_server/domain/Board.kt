package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
동아리 내 게시판 정보를 관리하는 엔티티
- 각 동아리별 게시판 생성 및 관리
- 게시판 유형 (일반, 공지, 팁) 구분
- 게시판 활성화 상태 및 설명 정보
 */

@Entity
@Table(name = "board")
data class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    val boardId: Long = 0L,

    @Column(name = "club_id", nullable = false)
    val clubId: Long,

    @Column(name = "name", nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: BoardType = BoardType.general,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)

enum class BoardType {
    general, notice, tips
}