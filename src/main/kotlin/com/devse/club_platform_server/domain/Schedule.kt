package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
동아리 일정 정보를 관리하는 엔티티
- 동아리별 일정 등록 및 관리
- 일정 생성 시 동아리 멤버들에게 알림 발송
- 일정 당일 리마인더 알림 기능
 */

@Entity
@Table(name = "schedule")
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    val scheduleId: Long = 0L,

    @Column(name = "club_id", nullable = false)
    val clubId: Long,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime,

    @Column(name = "end_date")
    val endDate: LocalDateTime? = null,

    @Column(name = "all_day", nullable = false)
    val allDay: Boolean = false,

    @Column(name = "created_by", nullable = false)
    val createdBy: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
)