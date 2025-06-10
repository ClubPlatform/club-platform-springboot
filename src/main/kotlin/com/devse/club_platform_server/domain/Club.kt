package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

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