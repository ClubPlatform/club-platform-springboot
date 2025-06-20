package com.devse.club_platform_server.dto.request

import com.devse.club_platform_server.domain.*
import jakarta.validation.constraints.*

/*
동아리 생성 요청 DTO
- 사용자가 새로운 동아리를 개설할 때 사용
- 동아리 기본정보, 운영정보, 가입방식 등 전체 설정 포함
 */
data class CreateClubRequest(
    @field:NotBlank(message = "동아리명은 필수입니다")
    @field:Size(max = 100, message = "동아리명은 100자를 초과할 수 없습니다")
    val name: String,

    @field:Size(max = 1000, message = "동아리 설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    @field:NotBlank(message = "동아리 카테고리는 필수입니다")
    val category: String, // 학술, 체육, 문화, 종교, 봉사, 취미, 게임, 음악, 댄스, 여행

    @field:Size(max = 100, message = "소속 기관명은 100자를 초과할 수 없습니다")
    val organization: String? = null,

    val logoImage: String? = null, // Base64 인코딩된 이미지

    @field:Size(max = 255, message = "모임 장소는 255자를 초과할 수 없습니다")
    val meetingPlace: String? = null,

    @field:Size(max = 100, message = "모임 시간은 100자를 초과할 수 없습니다")
    val meetingTime: String? = null,

    @field:Min(value = 0, message = "회비는 0원 이상이어야 합니다")
    val fee: Int? = 0,

    @field:NotNull(message = "가입 방법은 필수입니다")
    val joinMethod: JoinMethod = JoinMethod.free,

    @field:NotNull(message = "공개 설정은 필수입니다")
    val visibility: String
)

/*
동아리 정보 수정 요청 DTO
- 동아리 소유자/스태프가 동아리 정보를 수정할 때 사용
- 선택적 필드로 구성되어 부분 업데이트 지원
 */
data class UpdateClubRequest(
    @field:Size(max = 100, message = "동아리명은 100자를 초과할 수 없습니다")
    val name: String? = null,

    @field:Size(max = 1000, message = "동아리 설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    val category: String? = null,

    @field:Size(max = 100, message = "소속 기관명은 100자를 초과할 수 없습니다")
    val organization: String? = null,

    val logoImage: String? = null, // Base64 인코딩된 이미지

    @field:Size(max = 255, message = "모임 장소는 255자를 초과할 수 없습니다")
    val meetingPlace: String? = null,

    @field:Size(max = 100, message = "모임 시간은 100자를 초과할 수 없습니다")
    val meetingTime: String? = null,

    @field:Min(value = 0, message = "회비는 0원 이상이어야 합니다")
    val fee: Int? = null,

    val joinMethod: JoinMethod? = null,

    val visibility: String? = null,

    val isActive: Boolean? = null
)

/*
동아리 검색 요청 DTO
- 사용자가 동아리 목록을 검색할 때 사용
- 키워드 검색 및 카테고리 필터링 지원
 */
data class ClubSearchRequest(
    val keyword: String? = null,
    val category: ClubCategory? = null,
)

/*
동아리 가입 요청 DTO
- 사용자가 초대코드를 통해 동아리에 가입할 때 사용
- 비공개 동아리 접근을 위한 인증 수단
 */
data class JoinClubRequest(
    @field:NotBlank(message = "초대 코드는 필수입니다")
    val inviteCode: String
)