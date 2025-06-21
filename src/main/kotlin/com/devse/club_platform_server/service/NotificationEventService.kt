package com.devse.club_platform_server.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/*
알림 이벤트 처리 서비스
- 각종 액션 발생 시 알림 생성 트리거
- 일정 리마인더 배치 처리
 */

@Service
class NotificationEventService(
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(NotificationEventService::class.java)

    // 댓글 작성 시 알림 생성 트리거
    fun triggerCommentNotification(commentId: Long) {
        logger.info("댓글 알림 트리거: commentId=$commentId")
        notificationService.createCommentNotification(commentId)
    }

    // 게시글 작성 시 알림 생성 트리거
    fun triggerPostNotification(postId: Long) {
        logger.info("게시글 알림 트리거: postId=$postId")
        notificationService.createPostNotification(postId)
    }

    // 일정 생성 시 알림 생성 트리거
    fun triggerScheduleNotification(scheduleId: Long) {
        logger.info("일정 알림 트리거: scheduleId=$scheduleId")
        notificationService.createScheduleNotification(scheduleId)
    }

    // 매일 오전 9시에 당일 일정 리마인더 알림 생성
    @Scheduled(cron = "0 0 9 * * *") // 초 분 시 일 월 요일
    fun sendTodayScheduleReminders() {
        logger.info("당일 일정 리마인더 배치 시작")
        notificationService.createTodayScheduleReminders()
        logger.info("당일 일정 리마인더 배치 완료")
    }

    // 매주 일요일 새벽 2시에 30일 이상 된 읽은 알림 삭제
    @Scheduled(cron = "0 0 2 * * SUN")
    fun cleanupOldNotifications() {
        logger.info("오래된 알림 정리 배치 시작")
        try {
            val cutoffDate = java.time.LocalDateTime.now().minusDays(30)
            val deletedCount = notificationService.deleteOldReadNotifications(cutoffDate)
            logger.info("오래된 알림 정리 배치 완료")
        } catch (e: Exception) {
            logger.error("오래된 알림 정리 실패", e)
        }
    }
}

/*
 // 현재 일정 관련 API가 구현되어 있는 상황이 아니라 추가를 못했습니다.
 // 이후 ScheduleService 내부 일정 저장 로직에 NotificationEventService 의존성 주입 후 밑의 트리거 호출 부분을 추가해 주세요.

@Service
@Transactional
class ScheduleService(
    ...
    private val notificationEventService: NotificationEventService // 의존성 주입
) {

...

fun createSchedule(request: CreateScheduleRequest, userId: Long): ScheduleResponse {
    val schedule = // ... 일정 저장 로직
    scheduleRepository.save(schedule)

    // 알림 트리거 호출
    notificationEventService.triggerScheduleNotification(schedule.scheduleId)

    return // ... 응답 반환
}
 */