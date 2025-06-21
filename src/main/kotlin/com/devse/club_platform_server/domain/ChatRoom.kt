// ChatRoom.kt
package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_room")
class ChatRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    val chatRoomId: Long = 0L,

    @Column(name = "name")
    val name: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: ChatRoomType,

    @Column(name = "last_message_at")
    var lastMessageAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    fun copy(
        chatRoomId: Long = this.chatRoomId,
        name: String? = this.name,
        type: ChatRoomType = this.type,
        lastMessageAt: LocalDateTime? = this.lastMessageAt,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime? = this.updatedAt
    ): ChatRoom {
        return ChatRoom(
            chatRoomId = chatRoomId,
            name = name,
            type = type,
            lastMessageAt = lastMessageAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

enum class ChatRoomType {
    personal, group
}