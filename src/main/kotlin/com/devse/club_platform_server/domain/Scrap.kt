package com.devse.club_platform_server.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "scrap",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "post_id"])]
)
data class Scrap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scrap_id")
    val scrapId: Long = 0L,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "scrapped_at", nullable = false)
    val scrappedAt: LocalDateTime = LocalDateTime.now()
)