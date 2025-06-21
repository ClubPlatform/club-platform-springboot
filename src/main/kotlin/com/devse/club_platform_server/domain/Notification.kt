package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/*
알림 정보를 관리하는 엔티티
- 댓글/대댓글, 공지사항, 일정, 키워드 관련 알림 저장
- 알림 읽음 상태 및 생성 시간 추적
 */

@Entity
@Table(name = "notification")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    val notificationId: Long = 0L,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: NotificationType,

    @Column(name = "entity_type", nullable = false, length = 50)
    val entityType: String, // "post", "club", "user" 등

    @Column(name = "entity_id", nullable = false)
    val entityId: Long,

    @Column(name = "message", nullable = false)
    val message: String,

    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    comment,  // 댓글/대댓글 알림
    post,     // 공지 게시글 알림
    schedule, // 일정 알림
    keyword   // 키워드 알림
}