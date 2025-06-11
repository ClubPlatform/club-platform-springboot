package com.devse.club_platform_server.dto.response

import java.time.LocalDateTime

// 댓글 정보
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

// 댓글 목록 응답
data class CommentListResponse(
    val success: Boolean,
    val message: String,
    val comments: List<CommentInfo> = emptyList()
)

// 댓글 작성 응답
data class CreateCommentResponse(
    val success: Boolean,
    val message: String,
    val commentId: Long? = null,
    val createdAt: LocalDateTime? = null
)

// 댓글 좋아요 토글 응답
data class CommentLikeToggleResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likeCount: Int
)