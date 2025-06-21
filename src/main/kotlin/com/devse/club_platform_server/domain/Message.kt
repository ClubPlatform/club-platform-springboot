// Message.kt
package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "message")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    val messageId: Long = 0L,

    @Column(name = "chat_room_id", nullable = false)
    val chatRoomId: Long,

    @Column(name = "sender_id", nullable = false)
    val senderId: Long,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,  // var로 변경 (삭제 시 "삭제된 메시지입니다"로 변경)

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    val messageType: MessageType = MessageType.text,

    @Column(name = "file_url")
    val fileUrl: String? = null,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,  // var로 변경 (삭제 상태 변경)

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null  // var로 변경 (수정 시간 업데이트)
) {
    fun copy(
        messageId: Long = this.messageId,
        chatRoomId: Long = this.chatRoomId,
        senderId: Long = this.senderId,
        content: String = this.content,
        messageType: MessageType = this.messageType,
        fileUrl: String? = this.fileUrl,
        isDeleted: Boolean = this.isDeleted,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime? = this.updatedAt
    ): Message {
        return Message(
            messageId = messageId,
            chatRoomId = chatRoomId,
            senderId = senderId,
            content = content,
            messageType = messageType,
            fileUrl = fileUrl,
            isDeleted = isDeleted,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

enum class MessageType {
    text, image, file, system
}