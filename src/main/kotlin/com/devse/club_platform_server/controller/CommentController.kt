package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.CommentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class CommentController(
    private val commentService: CommentService
) {

    private val logger = LoggerFactory.getLogger(CommentController::class.java)

    // 댓글 목록 조회
    @GetMapping("/{postId}/comments")
    fun getCommentList(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<CommentListResponse> {
        val userId = authentication.principal as Long
        logger.info("댓글 목록 조회 요청: postId=$postId, userId=$userId")

        return try {
            val response = commentService.getCommentList(postId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("댓글 목록 조회 실패: ${e.message}")

            val errorResponse = CommentListResponse(
                success = false,
                message = e.message ?: "댓글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
        authentication: Authentication
    ): ResponseEntity<CreateCommentResponse> {
        val userId = authentication.principal as Long
        logger.info("댓글 작성 요청: postId=$postId, userId=$userId")

        return try {
            val response = commentService.createComment(postId, request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("댓글 작성 실패: ${e.message}")

            val errorResponse = CreateCommentResponse(
                success = false,
                message = e.message ?: "댓글 작성에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 댓글 수정
    @PutMapping("/{postId}/comments/{commentId}")
    fun updateComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        @Valid @RequestBody request: UpdateCommentRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CommentInfo>> {
        val userId = authentication.principal as Long
        logger.info("댓글 수정 요청: postId=$postId, commentId=$commentId, userId=$userId")

        return try {
            val updatedComment = commentService.updateComment(postId, commentId, request, userId)
            ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", updatedComment))

        } catch (e: Exception) {
            logger.error("댓글 수정 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "댓글을 수정할 수 없습니다."))
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("댓글 삭제 요청: postId=$postId, commentId=$commentId, userId=$userId")

        return try {
            commentService.deleteComment(postId, commentId, userId)
            ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", ""))

        } catch (e: Exception) {
            logger.error("댓글 삭제 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "댓글을 삭제할 수 없습니다."))
        }
    }

    // 댓글 좋아요 토글
    @PostMapping("/{postId}/comments/{commentId}/like")
    fun toggleCommentLike(
        @PathVariable postId: Long,
        @PathVariable commentId: Long,
        authentication: Authentication
    ): ResponseEntity<CommentLikeToggleResponse> {
        val userId = authentication.principal as Long
        logger.info("댓글 좋아요 토글 요청: postId=$postId, commentId=$commentId, userId=$userId")

        return try {
            // request 파라미터 제거하고 단순 토글로 변경
            val response = commentService.toggleCommentLike(postId, commentId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("댓글 좋아요 토글 실패: ${e.message}")

            val errorResponse = CommentLikeToggleResponse(
                success = false,
                message = e.message ?: "댓글 좋아요 처리에 실패했습니다.",
                isLiked = false,
                likeCount = 0
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}