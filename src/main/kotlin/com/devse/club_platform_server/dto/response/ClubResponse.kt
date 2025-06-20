package com.devse.club_platform_server.dto.response

import com.devse.club_platform_server.domain.*
import java.time.LocalDateTime

/*
 동아리 상세 정보 응답 DTO
 - 동아리 목록/상세 조회 시 개별 동아리 정보를 담는 데이터 클래스
 */
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
    val inviteCode: String,
    val createdBy: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val memberCount: Long = 0,
    val myRole: MemberRole? = null, // 요청한 사용자의 역할
    val isJoined: Boolean = false // 요청한 사용자의 가입 여부
)

/*
동아리 생성 처리 결과 응답 DTO
- 새 동아리 개설 완료 후 결과를 반환할 때 사용
- 가입코드는 이제 필수 정보로 제공
 */
data class CreateClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null,
    val inviteCode: String? = null // 동아리 생성 성공 시 항상 포함
)

/*
동아리 목록 조회 응답 DTO
- 동아리 검색/목록 조회 결과를 반환할 때 사용
 */
data class ClubListResponse(
    val success: Boolean,
    val message: String,
    val clubs: List<ClubInfo> = emptyList(),
    val totalCount: Long = 0
)

/*
동아리 가입코드 조회 응답 DTO
- 동아리 멤버가 가입코드를 확인할 때 사용
 */
data class InviteCodeResponse(
    val success: Boolean,
    val message: String,
    val inviteCode: String? = null
)

/*
동아리 가입 처리 결과 응답 DTO
- 초대코드를 통한 동아리 가입 완료 후 결과를 반환할 때 사용
 */
data class JoinClubResponse(
    val success: Boolean,
    val message: String,
    val clubId: Long? = null,
    val role: MemberRole? = null
)