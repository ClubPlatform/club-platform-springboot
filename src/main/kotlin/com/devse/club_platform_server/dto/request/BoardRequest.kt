package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.*

/*
게시판 생성 요청 DTO
- 동아리 관리자가 새로운 게시판을 생성할 때 사용
- 게시판 이름, 유형, 설명 정보 포함
 */
data class CreateBoardRequest(
    @field:NotNull(message = "동아리 ID는 필수입니다")
    val clubId: Long,

    @field:NotBlank(message = "게시판 이름은 필수입니다")
    @field:Size(max = 100, message = "게시판 이름은 100자를 초과할 수 없습니다")
    val name: String,

    @field:NotBlank(message = "게시판 유형은 필수입니다")
    @field:Pattern(
        regexp = "^(general|notice|tips)$",
        message = "게시판 유형은 general, notice, tips 중 하나여야 합니다"
    )
    val type: String,

    @field:Size(max = 255, message = "게시판 설명은 255자를 초과할 수 없습니다")
    val description: String? = null
)

/*
게시판 수정 요청 DTO
- 동아리 관리자가 기존 게시판을 수정할 때 사용
- 선택적 필드로 구성되어 부분 업데이트 지원
 */
data class UpdateBoardRequest(
    @field:Size(max = 100, message = "게시판 이름은 100자를 초과할 수 없습니다")
    val name: String? = null,

    @field:Size(max = 255, message = "게시판 설명은 255자를 초과할 수 없습니다")
    val description: String? = null,

    val isActive: Boolean? = null
)