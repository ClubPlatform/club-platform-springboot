package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 댓글 작성 요청
data class CreateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    val content: String,

    val isAnonymous: Boolean = false,

    val parentId: Long? = null
)

// 댓글 수정 요청
data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    val content: String
)