// MessageReadStatus.kt
package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "message_read_status",
    uniqueConstraints = [UniqueConstraint(columnNames = ["message_id", "user_id"])]
)
class MessageReadStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "message_id", nullable = false)
    val messageId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "read_at", nullable = false)
    val readAt: LocalDateTime = LocalDateTime.now()
)