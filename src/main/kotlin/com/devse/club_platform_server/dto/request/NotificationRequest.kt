package com.devse.club_platform_server.dto.request

import com.devse.club_platform_server.domain.NotificationType
import jakarta.validation.constraints.NotBlank

/*
알림 생성 요청 DTO
- 시스템에서 내부적으로 알림 생성 시 사용
 */
data class CreateNotificationRequest(
    val userId: Long,
    val type: NotificationType,
    val entityType: String,
    val entityId: Long,
    val message: String
)

/*
알림 키워드 등록 요청 DTO
- 사용자가 알림받을 키워드를 등록할 때 사용
 */
data class AddKeywordRequest(
    @field:NotBlank(message = "키워드는 필수입니다")
    val keyword: String
)

