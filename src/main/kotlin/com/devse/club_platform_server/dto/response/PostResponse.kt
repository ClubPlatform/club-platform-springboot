package com.devse.club_platform_server.dto.response

import java.time.LocalDateTime

// 게시글 목록 항목
data class PostListItem(
    val postId: Long,
    val title: String,
    val content: String?,
    val authorName: String,
    val createdAt: LocalDateTime,
    val viewCount: Int,
    val commentCount: Int
)

// 게시글 목록 응답
data class PostListResponse(
    val success: Boolean,
    val message: String,
    val posts: List<PostListItem> = emptyList()
)

// 게시글 상세 정보
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
    val canDelete: Boolean
)

// 게시글 상세 응답
data class PostDetailResponse(
    val success: Boolean,
    val message: String,
    val post: PostDetailInfo? = null
)

// 게시글 작성 응답
data class CreatePostResponse(
    val success: Boolean,
    val message: String,
    val postId: Long? = null,
    val createdAt: LocalDateTime? = null
)

// 게시글 수정 응답
data class UpdatePostResponse(
    val success: Boolean,
    val message: String,
    val updatedAt: LocalDateTime? = null
)

// 좋아요 토글 응답
data class LikeToggleResponse(
    val success: Boolean,
    val message: String,
    val isLiked: Boolean,
    val likeCount: Int
)

// 스크랩 토글 응답
data class ScrapToggleResponse(
    val success: Boolean,
    val message: String,
    val isScraped: Boolean,
)