package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/*
게시글 작성 요청 DTO
- 사용자가 동아리 게시판에 새 게시글을 작성할 때 사용
- 익명 작성, 공지글 설정 등 다양한 게시 옵션 지원
- 제목/내용 길이 제한을 통한 적절한 게시판 환경 조성
 */
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

/*
게시글 수정 요청 DTO
- 게시글 작성자가 기존 게시글을 수정할 때 사용
- 선택적 필드로 구성되어 부분 업데이트 지원
- 익명 설정은 수정 불가 (작성 시에만 결정)
 */
data class UpdatePostRequest(
    @field:Size(max = 255, message = "제목은 255자를 초과할 수 없습니다")
    val title: String? = null,

    @field:Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다")
    val content: String? = null,

    val isNotice: Boolean? = null
)

/*
게시글 좋아요 토글 요청 DTO
- 사용자가 게시글에 좋아요를 추가/취소할 때 사용
- 현재 좋아요 상태를 기반으로 토글 동작 수행
 */
data class LikeToggleRequest(
    val isLiked: Boolean
)