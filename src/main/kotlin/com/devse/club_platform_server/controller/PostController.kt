package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.PostService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {

    private val logger = LoggerFactory.getLogger(PostController::class.java)

    // 게시글 작성
    @PostMapping
    fun createPost(
        @Valid @RequestBody request: CreatePostRequest,
        authentication: Authentication
    ): ResponseEntity<CreatePostResponse> {
        val userId = authentication.principal as Long
        logger.info("게시글 작성 요청: boardId=${request.boardId}, userId=$userId")

        return try {
            val response = postService.createPost(request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시글 작성 실패: ${e.message}")

            val errorResponse = CreatePostResponse(
                success = false,
                message = e.message ?: "게시글 작성에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 특정 게시판의 게시글 목록 조회
    @GetMapping("/board/{boardId}")
    fun getBoardPosts(
        @PathVariable boardId: Long,
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("게시판 게시글 목록 조회 요청: boardId=$boardId, userId=$userId")

        return try {
            val response = postService.getBoardPosts(boardId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시판 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 인기 게시글 목록 조회 (내 모든 동아리 대상)
    @GetMapping("/hot")
    fun getHotPosts(
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("인기 게시글 목록 조회 요청: userId=$userId")

        return try {
            val response = postService.getHotPosts(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("인기 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "인기 게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 베스트 게시글 목록 조회 (내 모든 동아리 대상)
    @GetMapping("/best")
    fun getBestPosts(
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("베스트 게시글 목록 조회 요청: userId=$userId")

        return try {
            val response = postService.getBestPosts(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("베스트 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "베스트 게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 내가 작성한 게시글 목록 조회
    @GetMapping("/my-posts")
    fun getMyPosts(
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("내 게시글 목록 조회 요청: userId=$userId")

        return try {
            val response = postService.getMyPosts(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("내 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "내 게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 내가 댓글 단 게시글 목록 조회
    @GetMapping("/my-comments")
    fun getMyCommentPosts(
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("내가 댓글 단 게시글 목록 조회 요청: userId=$userId")

        return try {
            val response = postService.getMyCommentPosts(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("내가 댓글 단 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "댓글 단 게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 내가 스크랩한 게시글 목록 조회
    @GetMapping("/my-scraps")
    fun getMyScrappedPosts(
        authentication: Authentication
    ): ResponseEntity<PostListResponse> {
        val userId = authentication.principal as Long
        logger.info("내가 스크랩한 게시글 목록 조회 요청: userId=$userId")

        return try {
            val response = postService.getMyScrappedPosts(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("내가 스크랩한 게시글 목록 조회 실패: ${e.message}")

            val errorResponse = PostListResponse(
                success = false,
                message = e.message ?: "스크랩한 게시글 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    fun getPostDetail(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<PostDetailResponse> {
        val userId = authentication.principal as Long
        logger.info("게시글 상세 조회 요청: postId=$postId, userId=$userId")

        return try {
            val response = postService.getPostDetail(postId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시글 상세 조회 실패: ${e.message}")

            val errorResponse = PostDetailResponse(
                success = false,
                message = e.message ?: "게시글을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @Valid @RequestBody request: UpdatePostRequest,
        authentication: Authentication
    ): ResponseEntity<UpdatePostResponse> {
        val userId = authentication.principal as Long
        logger.info("게시글 수정 요청: postId=$postId, userId=$userId")

        return try {
            val response = postService.updatePost(postId, request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시글 수정 실패: ${e.message}")

            val errorResponse = UpdatePostResponse(
                success = false,
                message = e.message ?: "게시글을 수정할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("게시글 삭제 요청: postId=$postId, userId=$userId")

        return try {
            postService.deletePost(postId, userId)
            ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", ""))

        } catch (e: Exception) {
            logger.error("게시글 삭제 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "게시글을 삭제할 수 없습니다."))
        }
    }

    // 게시글 좋아요 토글
    @PostMapping("/{postId}/like")
    fun togglePostLike(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<LikeToggleResponse> {
        val userId = authentication.principal as Long
        logger.info("게시글 좋아요 토글 요청: postId=$postId, userId=$userId")

        return try {
            val response = postService.togglePostLike(postId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시글 좋아요 토글 실패: ${e.message}")

            val errorResponse = LikeToggleResponse(
                success = false,
                message = e.message ?: "좋아요 처리에 실패했습니다.",
                isLiked = false,
                likeCount = 0
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시글 스크랩 토글
    @PostMapping("/{postId}/scrap")
    fun togglePostScrap(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<ScrapToggleResponse> {
        val userId = authentication.principal as Long
        logger.info("게시글 스크랩 토글 요청: postId=$postId, userId=$userId")

        return try {
            val response = postService.togglePostScrap(postId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시글 스크랩 토글 실패: ${e.message}")

            val errorResponse = ScrapToggleResponse(
                success = false,
                message = e.message ?: "스크랩 처리에 실패했습니다.",
                isScraped = false
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}