package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.MemberRole
import com.devse.club_platform_server.domain.MemberStatus
import java.time.LocalDateTime

/*
동아리 멤버 상세 정보 응답 DTO
- 동아리 멤버 목록 조회 시 개별 멤버의 상세 정보를 담는 데이터 클래스
 */
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

/*
동아리 멤버 목록 조회 응답 DTO
- 특정 동아리의 전체 멤버 목록을 반환할 때 사용
 */
data class ClubMemberListResponse(
    val success: Boolean,
    val message: String,
    val members: List<ClubMemberInfo> = emptyList(),
    val totalMembers: Long = 0
)

/*
멤버 역할 변경 처리 결과 응답 DTO
- 동아리 관리자가 멤버 권한을 변경한 후 결과를 반환할 때 사용
 */
data class UpdateMemberRoleResponse(
    val success: Boolean,
    val message: String,
    val memberId: Long? = null,
    val newRole: MemberRole? = null
)

/*
멤버 강퇴 처리 결과 응답 DTO
- 동아리에서 회원을 강퇴한 후 결과를 반환할 때 사용
 */
data class RemoveMemberResponse(
    val success: Boolean,
    val message: String,
    val removedUserId: Long? = null
)

/*
동아리 탈퇴 처리 결과 응답 DTO
- 사용자가 자발적으로 동아리를 탈퇴한 후 결과를 반환할 때 사용
 */
data class LeaveClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null
)

/*
사용자의 동아리 멤버십 정보 응답 DTO
- 내가 가입한 동아리 목록 조회 시 개별 동아리 정보를 담는 데이터 클래스
 */
data class MyClubMembershipInfo(
    val clubId: Long,
    val clubName: String,
    val clubLogoImage: String?,
    val role: MemberRole,
    val status: MemberStatus,
    val joinedAt: LocalDateTime,
    val memberCount: Long
)

/*
내가 가입한 동아리 목록 조회 응답 DTO
- 사용자의 전체 동아리 가입 현황을 반환할 때 사용
 */
data class MyClubListResponse(
    val success: Boolean,
    val message: String,
    val clubs: List<MyClubMembershipInfo> = emptyList()
)
