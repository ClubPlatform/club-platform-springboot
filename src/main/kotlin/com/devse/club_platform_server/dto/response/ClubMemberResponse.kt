package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.MemberRole
import com.devse.club_platform_server.domain.MemberStatus
import java.time.LocalDateTime

// 멤버 정보 응답
data class ClubMemberInfo(
    val memberId: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val university: String,
    val department: String,
    val major: String,
    val studentId: String,
    val profileImage: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val joinedAt: LocalDateTime
)

// 멤버 목록 응답
data class ClubMemberListResponse(
    val success: Boolean,
    val message: String,
    val members: List<ClubMemberInfo> = emptyList(),
    val totalMembers: Long = 0
)

// 멤버 역할 변경 응답
data class UpdateMemberRoleResponse(
    val success: Boolean,
    val message: String,
    val memberId: Long? = null,
    val newRole: MemberRole? = null
)

// 멤버 강퇴 응답
data class RemoveMemberResponse(
    val success: Boolean,
    val message: String,
    val removedUserId: Long? = null
)

// 동아리 탈퇴 응답
data class LeaveClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null
)

// 내 동아리 멤버십 정보
data class MyClubMembershipInfo(
    val clubId: Long,
    val clubName: String,
    val clubLogoImage: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val joinedAt: LocalDateTime,
    val memberCount: Long
)

// 내 동아리 목록 응답
data class MyClubListResponse(
    val success: Boolean,
    val message: String,
    val clubs: List<MyClubMembershipInfo> = emptyList()
)
