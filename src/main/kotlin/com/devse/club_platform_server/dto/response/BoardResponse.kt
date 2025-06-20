package com.devse.club_platform_server.dto.response
/*
게시판 기본 정보 응답 DTO
- 동아리 게시판 목록 조회 시 개별 게시판 정보를 담는 데이터 클래스
 */
data class BoardInfo(
    val boardId: Long,
    val type: String, // BoardType을 문자열로 변환
    val name: String
)

/*
게시판 목록 조회 응답 DTO
- 특정 동아리의 전체 게시판 목록을 반환할 때 사용
 */
data class BoardListResponse(
    val success: Boolean,
    val message: String,
    val boards: List<BoardInfo> = emptyList()
)