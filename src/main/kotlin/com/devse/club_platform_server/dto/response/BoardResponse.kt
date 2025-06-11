package com.devse.club_platform_server.dto.response

// 게시판 정보 응답
data class BoardInfo(
    val boardId: Long,
    val type: String, // BoardType을 문자열로 변환
    val name: String
)

// 게시판 목록 응답
data class BoardListResponse(
    val success: Boolean,
    val message: String,
    val boards: List<BoardInfo> = emptyList()
)