package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
동아리 정보를 관리하는 엔티티
- 동아리 기본 정보 (이름, 설명, 카테고리, 학과, 동아리 로고)
- 모임 장소, 시간, 회비 등 운영 정보
- 가입 방식 (자유가입/승인제)
- 동아리 생성자 및 활성화 상태 관리
- 동아리별 고유 가입코드 (영문+숫자 8자리)
 */

@Entity
@Table(name = "club")
data class Club(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    val clubId: Long = 0L,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "category")
    val category: Int? = null,  // club_category 테이블 참조

    @Column(name = "organization", length = 100)
    val organization: String? = null,

    @Column(name = "logo_image", length = 255)
    val logoImage: String? = null,

    @Column(name = "meeting_place", length = 255)
    val meetingPlace: String? = null,

    @Column(name = "meeting_time", length = 100)
    val meetingTime: String? = null,

    @Column(name = "fee")
    val fee: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "join_method", nullable = false, columnDefinition = "ENUM('free','approval')")
    val joinMethod: JoinMethod = JoinMethod.approval,

    @Column(name = "visibility", length = 255)
    val visibility: String? = null,

    @Column(name = "invite_code", nullable = false, unique = true, length = 8)
    val inviteCode: String,

    @Column(name = "created_by", nullable = false)
    val createdBy: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "is_active", nullable = false, columnDefinition = "BIT(1)")
    val isActive: Boolean = true
)

enum class JoinMethod {
    free,      // 자유가입
    approval  // 승인제
}