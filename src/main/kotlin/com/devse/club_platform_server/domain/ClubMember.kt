package com.devse.club_platform_server.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/*
동아리 회원 정보를 관리하는 엔티티
- 사용자와 동아리 간의 멤버십 관계 저장
- 회원 역할 (소유자, 스태프, 일반회원) 구분
- 회원 상태 (활성, 비활성) 및 가입일 추적
 */

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
