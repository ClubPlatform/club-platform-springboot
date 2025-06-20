package com.devse.club_platform_server.dto.response

import java.time.LocalDateTime

/*
댓글 상세 정보 응답 DTO
- 댓글 목록 조회 시 개별 댓글 정보를 담는 데이터 클래스
 */
data class CommentInfo(
    val commentId: Long,
    val content: String,
    val authorName: String,
    val createdAt: LocalDateTime,
    val likeCount: Int,
    val isLiked: Boolean,
    val isAnonymous: Boolean,
    val parentId: Long?,
    val canEdit: Boolean,
    val canDelete: Boolean
)

/*
댓글 목록 조회 응답 DTO
- 특정 게시글의 전체 댓글을 반환할 때 사용
 */
data class CommentListResponse(
    val success: Boolean,
    val message: String,
    val comments: List<CommentInfo> = emptyList()
)

/*
댓글 작성 처리 결과 응답 DTO
- 새 댓글 작성 완료 후 결과를 반환할 때 사용
 */
data class CreateCommentResponse(
    val success: Boolean,
    val message: String,
    val commentId: Long? = null,
    val createdAt: LocalDateTime? = null
)

/*
댓글 좋아요 토글 처리 결과 응답 DTO
- 댓글 좋아요 추가/취소 후 결과를 반환할 때 사용
 */
data class CommentLikeToggleResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likeCount: Int
)