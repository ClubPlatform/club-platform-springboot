// ChatRequest.kt
package com.devse.club_platform_server.dto.request

import com.devse.club_platform_server.domain.MessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

// 채팅방 생성 요청
data class CreateChatRoomRequest(
    @field:Size(max = 255, message = "채팅방 이름은 255자를 초과할 수 없습니다")
    val name: String? = null,

    @field:NotNull(message = "채팅방 타입은 필수입니다")
    val type: String, // "personal" or "group"

    val memberIds: List<Long> = emptyList() // 초대할 멤버 ID 목록
)

// 메시지 전송 요청
data class SendMessageRequest(
    @field:NotNull(message = "채팅방 ID는 필수입니다")
    val chatRoomId: Long,

    @field:NotBlank(message = "메시지 내용은 필수입니다")
    @field:Size(max = 5000, message = "메시지는 5000자를 초과할 수 없습니다")
    val content: String,

    val messageType: MessageType = MessageType.text,

    val fileUrl: String? = null
)

// 메시지 수정 요청
data class UpdateMessageRequest(
    @field:NotBlank(message = "메시지 내용은 필수입니다")
    @field:Size(max = 5000, message = "메시지는 5000자를 초과할 수 없습니다")
    val content: String
)

// 채팅방 나가기 요청
data class LeaveChatRoomRequest(
    val reason: String? = null
)

// 메시지 읽음 처리 요청
data class MarkMessagesAsReadRequest(
    val messageIds: List<Long> = emptyList()
)
