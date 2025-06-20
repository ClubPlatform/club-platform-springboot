package com.devse.club_platform_server.dto.request

// 향후 게시판 생성/수정 기능 추가시 사용

/*
게시판 생성 요청 DTO
- 동아리 관리자가 새로운 게시판을 생성할 때 사용
- 게시판 이름, 유형, 설명 정보 포함
 */
data class CreateBoardRequest(
    val name: String,
    val type: String,
    val description: String? = null
)

/*
게시판 수정 요청 DTO
- 동아리 관리자가 기존 게시판을 수정할 때 사용
- 선택적 필드로 구성되어 부분 업데이트 지원
 */
data class UpdateBoardRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)