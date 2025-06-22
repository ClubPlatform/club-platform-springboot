package com.devse.club_platform_server.dto.response

import java.time.LocalDateTime

/*
게시글 목록 항목 정보 응답 DTO (개선됨)
- 게시판 내 게시글 목록 조회 시 개별 게시글 요약정보를 담는 데이터 클래스
- boardId, boardName 추가로 전역 필터링 시 게시글 출처 표시 가능
 */
data class PostListItem(
    val postId: Long,
    val title: String,
    val content: String?,
    val authorName: String,
    val createdAt: LocalDateTime,
    val viewCount: Int,
    val commentCount: Int,
    val boardId: Long,           // 추가: 게시글이 속한 게시판 ID
    val boardName: String        // 추가: 게시글이 속한 게시판 이름
)

/*
게시글 목록 조회 응답 DTO (개선됨)
- 특정 게시판 또는 전역 필터링된 게시글 목록을 반환할 때 사용
- totalCount 추가로 조회된 게시글 총 개수 표시
 */
data class PostListResponse(
    val success: Boolean,
    val message: String,
    val totalCount: Long = 0,    // 추가: 조회된 게시글 총 개수
    val posts: List<PostListItem> = emptyList()
)

/*
게시글 상세 정보 응답 DTO (개선됨)
- 게시글 상세보기 시 전체 게시글 정보를 담는 데이터 클래스
- boardId, boardName 추가로 상세 페이지에서도 게시판 정보 확인 가능
 */
data class PostDetailInfo(
    val postId: Long,
    val title: String,
    val content: String?,
    val authorName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val viewCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val isScraped: Boolean,
    val isAnonymous: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val boardId: Long,           // 추가: 게시글이 속한 게시판 ID
    val boardName: String        // 추가: 게시글이 속한 게시판 이름
)

/*
게시글 상세 조회 응답 DTO
- 특정 게시글의 전체 정보를 반환할 때 사용
 */
data class PostDetailResponse(
    val success: Boolean,
    val message: String,
    val post: PostDetailInfo? = null
)

/*
게시글 작성 처리 결과 응답 DTO
- 새 게시글 작성 완료 후 결과를 반환할 때 사용
 */
data class CreatePostResponse(
    val success: Boolean,
    val message: String,
    val postId: Long? = null,
    val createdAt: LocalDateTime? = null
)

/*
게시글 수정 처리 결과 응답 DTO
- 기존 게시글 수정 완료 후 결과를 반환할 때 사용
 */
data class UpdatePostResponse(
    val success: Boolean,
    val message: String,
    val updatedAt: LocalDateTime? = null
)

/*
게시글 좋아요 토글 처리 결과 응답 DTO
- 게시글 좋아요 추가/취소 후 결과를 반환할 때 사용
 */
data class LikeToggleResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likeCount: Int
)

/*
게시글 스크랩 토글 처리 결과 응답 DTO
- 게시글 북마크 추가/취소 후 결과를 반환할 때 사용
 */
data class ScrapToggleResponse(
    val success: Boolean,
    val message: String,
    val isScraped: Boolean
)