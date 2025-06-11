package com.devse.club_platform_server.dto.request

// 게시판 관련 요청은 현재 없음 (조회만 존재)
// 향후 게시판 생성/수정 기능 추가시 사용

data class CreateBoardRequest(
    val name: String,
    val type: String,
    val description: String? = null
)

data class UpdateBoardRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)