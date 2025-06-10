package com.devse.club_platform_server.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "club_member")
data class ClubMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "club_id", nullable = false)
    val clubId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: MemberRole,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MemberStatus = MemberStatus.active,

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now()
)

enum class MemberStatus {
    active, inactive
}

enum class MemberRole {
    owner, staff, member
}
