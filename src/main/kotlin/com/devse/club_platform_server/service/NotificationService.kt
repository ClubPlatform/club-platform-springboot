package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/*
알림 관리 서비스
- 알림 생성, 조회, 삭제 기능
- 실시간 알림 생성 및 배치 처리
- 키워드 기반 알림 시스템
 */

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userKeywordRepository: UserKeywordRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val scheduleRepository: ScheduleRepository,
    private val boardRepository: BoardRepository
) {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    // 알림 목록 조회
    @Transactional(readOnly = true)
    fun getNotifications(
        userId: Long,
        type: NotificationType?,
        page: Int = 0,
        size: Int = 20
    ): NotificationListResponse {
        val pageable: Pageable = PageRequest.of(page, size)

        val notificationsPage = if (type != null) {
            notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
        } else {
            notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        }

        val notificationInfos = notificationsPage.content.map { notification ->
            NotificationInfo(
                notificationId = notification.notificationId,
                type = notification.type,
                entityType = notification.entityType,
                entityId = notification.entityId,
                message = notification.message,
                isRead = notification.isRead,
                createdAt = notification.createdAt
            )
        }

        // 읽지 않은 알림 개수 조회
        val unreadCount = if (type != null) {
            notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, type)
        } else {
            notificationRepository.countByUserIdAndIsReadFalse(userId)
        }

        return NotificationListResponse(
            success = true,
            message = "알림 목록 조회 성공",
            notifications = notificationInfos,
            totalElements = notificationsPage.totalElements,
            totalPages = notificationsPage.totalPages,
            currentPage = page,
            pageSize = size,
            unreadCount = unreadCount
        )
    }

    // 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): NotificationCountResponse {
        val totalUnreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId)
        val commentUnreadCount = notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, NotificationType.comment)
        val postUnreadCount = notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, NotificationType.post)
        val scheduleUnreadCount = notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, NotificationType.schedule)
        val keywordUnreadCount = notificationRepository.countByUserIdAndTypeAndIsReadFalse(userId, NotificationType.keyword)

        return NotificationCountResponse(
            success = true,
            message = "읽지 않은 알림 개수 조회 성공",
            totalUnreadCount = totalUnreadCount,
            commentUnreadCount = commentUnreadCount,
            postUnreadCount = postUnreadCount,
            scheduleUnreadCount = scheduleUnreadCount,
            keywordUnreadCount = keywordUnreadCount
        )
    }

    // 알림 읽음 처리
    fun markAsRead(notificationId: Long, userId: Long): ApiResponse<String> {
        val notification = notificationRepository.findByIdOrNull(notificationId)
            ?: throw IllegalArgumentException("존재하지 않는 알림입니다.")

        if (notification.userId != userId) {
            throw IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.")
        }

        notificationRepository.markAsRead(notificationId, userId)

        return ApiResponse.success("알림이 읽음 처리되었습니다.")
    }

    // 모든 알림 읽음 처리
    fun markAllAsRead(userId: Long, type: NotificationType?): ApiResponse<String> {
        if (type != null) {
            notificationRepository.markAllAsReadByType(userId, type)
        } else {
            notificationRepository.markAllAsRead(userId)
        }

        val message = if (type != null) "${type.name} 알림이 모두 읽음 처리되었습니다." else "모든 알림이 읽음 처리되었습니다."
        return ApiResponse.success(message)
    }

    // 알림 삭제
    fun deleteNotification(notificationId: Long, userId: Long): ApiResponse<String> {
        val deletedCount = notificationRepository.deleteByNotificationIdAndUserId(notificationId, userId)

        if (deletedCount == 0) {
            throw IllegalArgumentException("삭제할 알림이 없거나 권한이 없습니다.")
        }

        return ApiResponse.success("알림이 삭제되었습니다.")
    }

    // 댓글/대댓글 알림 생성
    @Async
    fun createCommentNotification(commentId: Long) {
        try {
            val comment = commentRepository.findByIdOrNull(commentId) ?: return
            val post = postRepository.findByIdOrNull(comment.postId) ?: return

            // 게시글 작성자에게 알림 (본인이 댓글 작성한 경우 제외)
            if (post.authorId != comment.authorId) {
                val message = if (comment.parentId != null) {
                    "내 댓글에 대댓글이 달렸습니다: \"${post.title}\""
                } else {
                    "내 게시글에 댓글이 달렸습니다: \"${post.title}\""
                }

                createNotification(
                    CreateNotificationRequest(
                        userId = post.authorId,
                        type = NotificationType.comment,
                        entityType = "post",
                        entityId = post.postId,
                        message = message
                    )
                )
            }

            // 대댓글인 경우, 부모 댓글 작성자에게도 알림
            if (comment.parentId != null) {
                val parentComment = commentRepository.findByIdOrNull(comment.parentId) ?: return
                if (parentComment.authorId != comment.authorId && parentComment.authorId != post.authorId) {
                    createNotification(
                        CreateNotificationRequest(
                            userId = parentComment.authorId,
                            type = NotificationType.comment,
                            entityType = "post",
                            entityId = post.postId,
                            message = "내 댓글에 대댓글이 달렸습니다: \"${post.title}\""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("댓글 알림 생성 실패: commentId=$commentId", e)
        }
    }

    // 공지사항 알림 생성
    @Async
    fun createPostNotification(postId: Long) {
        try {
            val post = postRepository.findByIdOrNull(postId) ?: return
            val board = boardRepository.findByIdOrNull(post.boardId) ?: return

            // 공지사항만 알림 생성
            if (board.type == BoardType.notice) {
                // 해당 동아리의 모든 활성 멤버에게 알림
                val activeMembers = clubMemberRepository.findByClubIdAndStatusOrderByJoinedAtDesc(
                    board.clubId, MemberStatus.active
                )

                activeMembers.forEach { member ->
                    // 게시글 작성자는 제외
                    if (member.userId != post.authorId) {
                        createNotification(
                            CreateNotificationRequest(
                                userId = member.userId,
                                type = NotificationType.post,
                                entityType = "post",
                                entityId = post.postId,
                                message = "새로운 공지사항이 등록되었습니다: \"${post.title}\""
                            )
                        )
                    }
                }
            }

            // 키워드 알림도 함께 처리
            createKeywordNotification(postId)
        } catch (e: Exception) {
            logger.error("게시글 알림 생성 실패: postId=$postId", e)
        }
    }

    // 키워드 알림 생성
    @Async
    fun createKeywordNotification(postId: Long) {
        try {
            val post = postRepository.findByIdOrNull(postId) ?: return
            val searchText = "${post.title} ${post.content ?: ""}"

            // 키워드가 포함된 텍스트인지 확인하고 해당 사용자들에게 알림
            val keywordUsers = userKeywordRepository.findUsersWithKeywordsInText(searchText)

            keywordUsers.forEach { userKeyword ->
                // 게시글 작성자는 제외
                if (userKeyword.userId != post.authorId) {
                    createNotification(
                        CreateNotificationRequest(
                            userId = userKeyword.userId,
                            type = NotificationType.keyword,
                            entityType = "post",
                            entityId = post.postId,
                            message = "관심 키워드 '${userKeyword.keyword}'가 포함된 게시글이 등록되었습니다: \"${post.title}\""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("키워드 알림 생성 실패: postId=$postId", e)
        }
    }

    // 일정 알림 생성
    @Async
    fun createScheduleNotification(scheduleId: Long) {
        try {
            val schedule = scheduleRepository.findByIdOrNull(scheduleId) ?: return

            // 해당 동아리의 모든 활성 멤버에게 알림
            val activeMembers = clubMemberRepository.findByClubIdAndStatusOrderByJoinedAtDesc(
                schedule.clubId, MemberStatus.active
            )

            activeMembers.forEach { member ->
                createNotification(
                    CreateNotificationRequest(
                        userId = member.userId,
                        type = NotificationType.schedule,
                        entityType = "schedule",
                        entityId = schedule.scheduleId,
                        message = "새로운 일정이 등록되었습니다: \"${schedule.title}\""
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("일정 알림 생성 실패: scheduleId=$scheduleId", e)
        }
    }

    // 당일 일정 리마인더 알림 생성 (배치용)
    fun createTodayScheduleReminders() {
        try {
            val today = LocalDateTime.now()
            val todaySchedules = scheduleRepository.findTodaySchedules(today)

            todaySchedules.forEach { schedule ->
                val activeMembers = clubMemberRepository.findByClubIdAndStatusOrderByJoinedAtDesc(
                    schedule.clubId, MemberStatus.active
                )

                activeMembers.forEach { member ->
                    createNotification(
                        CreateNotificationRequest(
                            userId = member.userId,
                            type = NotificationType.schedule,
                            entityType = "schedule",
                            entityId = schedule.scheduleId,
                            message = "오늘 일정이 있습니다: \"${schedule.title}\""
                        )
                    )
                }
            }

            logger.info("당일 일정 리마인더 알림 생성 완료: ${todaySchedules.size}개 일정")
        } catch (e: Exception) {
            logger.error("당일 일정 리마인더 알림 생성 실패", e)
        }
    }

    // 오래된 읽은 알림 삭제 (배치 처리용)
    fun deleteOldReadNotifications(cutoffDate: LocalDateTime): Int {
        return try {
            // 30일 이상 된 읽은 알림만 삭제
            val deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate)
            logger.info("오래된 읽은 알림 삭제 완료: ${deletedCount}개")
            deletedCount
        } catch (e: Exception) {
            logger.error("오래된 알림 삭제 실패", e)
            0
        }
    }

    // 기본 알림 생성 메소드
    private fun createNotification(request: CreateNotificationRequest) {
        val notification = Notification(
            userId = request.userId,
            type = request.type,
            entityType = request.entityType,
            entityId = request.entityId,
            message = request.message,
            isRead = false,
            createdAt = LocalDateTime.now()
        )

        notificationRepository.save(notification)
    }
}