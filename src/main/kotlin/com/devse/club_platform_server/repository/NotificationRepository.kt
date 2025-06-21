package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Notification
import com.devse.club_platform_server.domain.NotificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    // 특정 사용자의 알림 목록 조회 (최신순)
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Notification>

    // 특정 사용자의 특정 타입 알림 조회
    fun findByUserIdAndTypeOrderByCreatedAtDesc(
        userId: Long,
        type: NotificationType,
    ): List<Notification>

    // 읽지 않은 알림 개수 조회
    fun countByUserIdAndIsReadFalse(userId: Long): Long

    // 특정 타입의 읽지 않은 알림 개수 조회
    fun countByUserIdAndTypeAndIsReadFalse(userId: Long, type: NotificationType): Long

    // 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId AND n.userId = :userId")
    fun markAsRead(@Param("notificationId") notificationId: Long, @Param("userId") userId: Long)

    // 모든 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    fun markAllAsRead(@Param("userId") userId: Long)

    // 특정 타입 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.type = :type")
    fun markAllAsReadByType(@Param("userId") userId: Long, @Param("type") type: NotificationType)

    // 알림 삭제 (본인만 가능)
    fun deleteByNotificationIdAndUserId(notificationId: Long, userId: Long): Int

    // 오래된 알림 삭제 (배치 처리용)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    fun deleteOldNotifications(@Param("cutoffDate") cutoffDate: java.time.LocalDateTime): Int

    // 오래된 읽은 알림만 삭제 (배치 처리용)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.isRead = true")
    fun deleteOldReadNotifications(@Param("cutoffDate") cutoffDate: java.time.LocalDateTime): Int
}