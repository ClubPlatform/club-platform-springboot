package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/*
댓글 작성 요청 DTO
- 사용자가 게시글에 댓글을 작성할 때 사용
- 익명 댓글 및 대댓글(답글) 기능 지원
 */
data class CreateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    val content: String,

    val isAnonymous: Boolean = false,

    val parentId: Long? = null
)

/*
댓글 수정 요청 DTO
- 댓글 작성자가 기존 댓글 내용을 수정할 때 사용
- 댓글 내용만 수정 가능 (익명 설정 등은 변경 불가)
 */
data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 필수입니다")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다")
    val content: String
)