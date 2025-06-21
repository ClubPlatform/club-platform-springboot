// ChatResponse.kt
package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.ChatRoomType
import com.devse.club_platform_server.domain.MessageType
import java.time.LocalDateTime

// 채팅방 정보
data class ChatRoomInfo(
    val chatRoomId: Long,
    val name: String?,
    val type: ChatRoomType,
    val memberCount: Long,
    val lastMessage: MessageInfo?,
    val unreadCount: Long,
    val createdAt: LocalDateTime,
    val members: List<ChatMemberInfo> = emptyList()
)

// 채팅 멤버 정보
data class ChatMemberInfo(
    val userId: Long,
    val userName: String,
    val profileImage: String?,
    val joinedAt: LocalDateTime,
    val lastReadAt: LocalDateTime?
)

// 메시지 정보
data class MessageInfo(
    val messageId: Long,
    val chatRoomId: Long,
    val senderId: Long,
    val senderName: String,
    val senderProfileImage: String?,
    val content: String,
    val messageType: MessageType,
    val fileUrl: String?,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val readCount: Long = 0,
    val isRead: Boolean = false
)

// 채팅방 목록 응답
data class ChatRoomListResponse(
    val success: Boolean,
    val message: String,
    val chatRooms: List<ChatRoomInfo> = emptyList()
)

// 채팅방 생성 응답
data class CreateChatRoomResponse(
    val success: Boolean,
    val message: String,
    val chatRoomId: Long? = null
)

// 메시지 목록 응답
data class MessageListResponse(
    val success: Boolean,
    val message: String,
    val messages: List<MessageInfo> = emptyList(),
    val hasNext: Boolean = false,
    val totalElements: Long = 0
)

// 메시지 전송 응답
data class SendMessageResponse(
    val success: Boolean,
    val message: String,
    val messageId: Long? = null,
    val createdAt: LocalDateTime? = null
)

// WebSocket 메시지 전송용 DTO
data class WebSocketMessage(
    val type: String, // "MESSAGE", "READ", "DELETE", "TYPING"
    val chatRoomId: Long,
    val senderId: Long,
    val senderName: String,
    val content: String? = null,
    val messageId: Long? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)