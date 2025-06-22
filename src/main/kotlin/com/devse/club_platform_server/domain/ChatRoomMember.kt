// ChatRoomMember.kt
package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_room_member",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["chat_room_id", "user_id"])
    ]
)
class ChatRoomMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val memberId: Long = 0L,

    @Column(name = "chat_room_id", nullable = false)
    val chatRoomId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_read_at")
    var lastReadAt: LocalDateTime? = null
) {
    fun copy(
        memberId: Long = this.memberId,
        chatRoomId: Long = this.chatRoomId,
        userId: Long = this.userId,
        joinedAt: LocalDateTime = this.joinedAt,
        lastReadAt: LocalDateTime? = this.lastReadAt
    ): ChatRoomMember {
        return ChatRoomMember(
            memberId = memberId,
            chatRoomId = chatRoomId,
            userId = userId,
            joinedAt = joinedAt,
            lastReadAt = lastReadAt
        )
    }
}