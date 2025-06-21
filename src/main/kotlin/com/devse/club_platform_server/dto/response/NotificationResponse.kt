package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.NotificationType
import java.time.LocalDateTime

/*
알림 상세 정보 응답 DTO
- 알림 목록 조회 시 개별 알림 정보를 담는 데이터 클래스
 */
data class NotificationInfo(
    val notificationId: Long,
    val type: NotificationType,
    val entityType: String,
    val entityId: Long,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

/*
알림 목록 조회 응답 DTO
- 페이징된 알림 목록을 반환할 때 사용
 */
data class NotificationListResponse(
    val success: Boolean,
    val message: String,
    val notifications: List<NotificationInfo> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val pageSize: Int = 0,
    val unreadCount: Long = 0
)

/*
알림 개수 조회 응답 DTO
- 읽지 않은 알림 개수를 반환할 때 사용
 */
data class NotificationCountResponse(
    val success: Boolean,
    val message: String,
    val totalUnreadCount: Long = 0,
    val commentUnreadCount: Long = 0,
    val postUnreadCount: Long = 0,
    val scheduleUnreadCount: Long = 0,
    val keywordUnreadCount: Long = 0
)

/*
키워드 목록 조회 응답 DTO
- 사용자가 등록한 키워드 목록을 반환할 때 사용
 */
data class KeywordListResponse(
    val success: Boolean,
    val message: String,
    val keywords: List<String> = emptyList()
)