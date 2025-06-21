package com.devse.club_platform_server.dto.response

import java.time.LocalDateTime

/*
게시판 기본 정보 응답 DTO
- 동아리 게시판 목록 조회 시 개별 게시판 정보를 담는 데이터 클래스
 */
data class BoardInfo(
    val boardId: Long,
    val type: String, // BoardType을 문자열로 변환
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
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

/*
게시판 생성 응답 DTO
- 게시판 생성 완료 후 결과를 반환할 때 사용
 */
data class CreateBoardResponse(
    val success: Boolean,
    val message: String,
    val boardId: Long? = null,
    val board: BoardInfo? = null
)

/*
게시판 수정 응답 DTO
- 게시판 수정 완료 후 결과를 반환할 때 사용
 */
data class UpdateBoardResponse(
    val success: Boolean,
    val message: String,
    val board: BoardInfo? = null
)

/*
게시판 삭제 응답 DTO
- 게시판 삭제 완료 후 결과를 반환할 때 사용
 */
data class DeleteBoardResponse(
    val success: Boolean,
    val message: String
)