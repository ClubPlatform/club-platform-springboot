package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

// 동아리 정보 응답
data class ClubInfo(
    val clubId: Long,
    val name: String,
    val description: String?,
    val category: ClubCategory?,
    val organization: String?,
    val logoImage: String?,
    val meetingPlace: String?,
    val meetingTime: String?,
    val fee: Int?,
    val joinMethod: JoinMethod,
    val visibility: String?,
    val createdBy: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val memberCount: Long = 0,
    val myRole: MemberRole? = null, // 요청한 사용자의 역할
    val isJoined: Boolean = false // 요청한 사용자의 가입 여부
)

// 동아리 생성 응답
data class CreateClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null,
    val inviteCode: String? = null // 초대 링크용 코드
)

// 동아리 목록 응답
data class ClubListResponse(
    val success: Boolean,
    val message: String,
    val clubs: List<ClubInfo> = emptyList(),
    val totalCount:Long = 0
)

// 동아리 초대 링크 생성 응답
data class InviteLinkResponse(
    val success: Boolean,
    val message: String,
    val inviteCode: String? = null,
    val inviteUrl: String? = null,
    val expiresAt: LocalDateTime? = null
)

// 동아리 가입 응답
data class JoinClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null,
    val role: MemberRole? = null
)