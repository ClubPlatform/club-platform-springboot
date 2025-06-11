package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

// 게시글 작성 요청
data class CreatePostRequest(
    @field:NotNull(message = "게시판 ID는 필수입니다")
    val boardId: Long,

    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    val title: String,

    @field:Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다")
    val content: String? = null,

    val isAnonymous: Boolean = false,

    val isNotice: Boolean = false
)

// 게시글 수정 요청
data class UpdatePostRequest(
    @field:Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    val title: String? = null,

    @field:Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다")
    val content: String? = null,

    val isNotice: Boolean? = null
)

// 좋아요 토글 요청
data class LikeToggleRequest(
    val isLiked: Boolean
)