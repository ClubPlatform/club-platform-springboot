package com.devse.club_platform_server.controller

import com.devse.club_platform_server.domain.NotificationType
import com.devse.club_platform_server.dto.request.AddKeywordRequest
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.NotificationService
import com.devse.club_platform_server.service.UserKeywordService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userKeywordService: UserKeywordService
) {

    private val logger = LoggerFactory.getLogger(NotificationController::class.java)

    // 알림 목록 조회
    @GetMapping
    fun getNotifications(
        @RequestParam(required = false) type: NotificationType?,
        authentication: Authentication
    ): ResponseEntity<NotificationListResponse> {
        val userId = authentication.principal as Long
        logger.info("알림 목록 조회: userId=$userId, type=$type")

        return try {
            val response = notificationService.getNotifications(userId, type)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("알림 목록 조회 실패: ${e.message}")

            val errorResponse = NotificationListResponse(
                success = false,
                message = e.message ?: "알림 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread-count")
    fun getUnreadCount(authentication: Authentication): ResponseEntity<NotificationCountResponse> {
        val userId = authentication.principal as Long
        logger.info("읽지 않은 알림 개수 조회: userId=$userId")

        return try {
            val response = notificationService.getUnreadCount(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("읽지 않은 알림 개수 조회 실패: ${e.message}")

            val errorResponse = NotificationCountResponse(
                success = false,
                message = e.message ?: "읽지 않은 알림 개수를 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    fun markAsRead(
        @PathVariable notificationId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("알림 읽음 처리: notificationId=$notificationId, userId=$userId")

        return try {
            val response = notificationService.markAsRead(notificationId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("알림 읽음 처리 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "알림을 읽음 처리할 수 없습니다."))
        }
    }

    // 모든 알림 읽음 처리
    @PutMapping("/read-all")
    fun markAllAsRead(
        @RequestParam(required = false) type: NotificationType?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("모든 알림 읽음 처리: userId=$userId, type=$type")

        return try {
            val response = notificationService.markAllAsRead(userId, type)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("모든 알림 읽음 처리 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "알림을 읽음 처리할 수 없습니다."))
        }
    }

    // 알림 삭제
    @DeleteMapping("/{notificationId}")
    fun deleteNotification(
        @PathVariable notificationId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("알림 삭제: notificationId=$notificationId, userId=$userId")

        return try {
            val response = notificationService.deleteNotification(notificationId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("알림 삭제 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "알림을 삭제할 수 없습니다."))
        }
    }

    // === 키워드 관리 API ===

    // 사용자 키워드 목록 조회
    @GetMapping("/keywords")
    fun getUserKeywords(authentication: Authentication): ResponseEntity<KeywordListResponse> {
        val userId = authentication.principal as Long
        logger.info("사용자 키워드 목록 조회: userId=$userId")

        return try {
            val response = userKeywordService.getUserKeywords(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("키워드 목록 조회 실패: ${e.message}")

            val errorResponse = KeywordListResponse(
                success = false,
                message = e.message ?: "키워드 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 키워드 추가
    @PostMapping("/keywords")
    fun addKeyword(
        @Valid @RequestBody request: AddKeywordRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("키워드 추가: userId=$userId, keyword=${request.keyword}")

        return try {
            val response = userKeywordService.addKeyword(userId, request)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("키워드 추가 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "키워드를 추가할 수 없습니다."))
        }
    }

    // 키워드 삭제
    @DeleteMapping("/keywords/{keyword}")
    fun deleteKeyword(
        @PathVariable keyword: String,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("키워드 삭제: userId=$userId, keyword=$keyword")

        return try {
            val response = userKeywordService.deleteKeyword(userId, keyword)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("키워드 삭제 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "키워드를 삭제할 수 없습니다."))
        }
    }
}