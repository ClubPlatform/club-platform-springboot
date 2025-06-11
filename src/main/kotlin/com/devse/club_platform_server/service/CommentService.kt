package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentLikeRepository: CommentLikeRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val boardService: BoardService
) {

    private val logger = LoggerFactory.getLogger(CommentService::class.java)

    // 댓글 작성
    fun createComment(postId: Long, request: CreateCommentRequest, userId: Long): CreateCommentResponse {
        logger.info("댓글 작성 요청: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        boardService.validateBoardAccess(post.boardId, userId)

        // 부모 댓글 존재 확인 (대댓글인 경우)
        if (request.parentId != null) {
            val parentComment = commentRepository.findById(request.parentId).orElseThrow {
                IllegalArgumentException("존재하지 않는 부모 댓글입니다.")
            }
            if (parentComment.postId != postId) {
                throw IllegalArgumentException("부모 댓글이 해당 게시글의 댓글이 아닙니다.")
            }
        }

        val comment = Comment(
            postId = postId,
            authorId = userId,
            parentId = request.parentId,
            content = request.content,
            isAnonymous = request.isAnonymous
        )

        val savedComment = commentRepository.save(comment)

        // 게시글의 댓글 수 업데이트
        val commentCount = commentRepository.countByPostId(postId).toInt()
        postRepository.updateCommentCount(postId, commentCount)

        logger.info("댓글 작성 완료: commentId=${savedComment.commentId}")

        return CreateCommentResponse(
            success = true,
            message = "댓글이 성공적으로 작성되었습니다.",
            commentId = savedComment.commentId,
            createdAt = savedComment.createdAt
        )
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    fun getCommentList(postId: Long, userId: Long): CommentListResponse {
        logger.info("댓글 목록 조회: postId=$postId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(post.boardId, userId)

        val comments = commentRepository.findByPostIdOrderByHierarchy(postId)

        val commentInfos = comments.map { comment ->
            val author = userRepository.findById(comment.authorId).orElse(null)
            val likeCount = commentLikeRepository.countByCommentId(comment.commentId).toInt()
            val isLiked = commentLikeRepository.existsByCommentIdAndUserId(comment.commentId, userId)

            // 수정/삭제 권한 확인
            val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            val canEdit = comment.authorId == userId
            val canDelete = comment.authorId == userId || 
                           (membership?.role == MemberRole.owner || membership?.role == MemberRole.staff)

            CommentInfo(
                commentId = comment.commentId,
                content = comment.content,
                authorName = if (comment.isAnonymous) "익명" else (author?.name ?: "알 수 없음"),
                createdAt = comment.createdAt,
                likeCount = likeCount,
                isLiked = isLiked,
                isAnonymous = comment.isAnonymous,
                parentId = comment.parentId,
                canEdit = canEdit,
                canDelete = canDelete
            )
        }

        logger.info("댓글 목록 조회 완료: postId=$postId, 댓글수=${commentInfos.size}")

        return CommentListResponse(
            success = true,
            message = "댓글 목록 조회 성공",
            comments = commentInfos
        )
    }

    // 댓글 수정
    fun updateComment(postId: Long, commentId: Long, request: UpdateCommentRequest, userId: Long): CommentInfo {
        logger.info("댓글 수정 요청: commentId=$commentId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("존재하지 않는 댓글입니다.")
        }

        if (comment.postId != postId) {
            throw IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.")
        }

        // 게시판 접근 권한 확인
        boardService.validateBoardAccess(post.boardId, userId)

        // 수정 권한 확인 (작성자만 수정 가능)
        if (comment.authorId != userId) {
            throw IllegalArgumentException("댓글을 수정할 권한이 없습니다.")
        }

        val updatedComment = comment.copy(
            content = request.content,
            updatedAt = LocalDateTime.now()
        )

        commentRepository.save(updatedComment)

        logger.info("댓글 수정 완료: commentId=$commentId")

        // 수정된 댓글 정보 반환
        val author = userRepository.findById(updatedComment.authorId).orElse(null)
        val likeCount = commentLikeRepository.countByCommentId(commentId).toInt()
        val isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)

        return CommentInfo(
            commentId = updatedComment.commentId,
            content = updatedComment.content,
            authorName = if (updatedComment.isAnonymous) "익명" else (author?.name ?: "알 수 없음"),
            createdAt = updatedComment.createdAt,
            likeCount = likeCount,
            isLiked = isLiked,
            isAnonymous = updatedComment.isAnonymous,
            parentId = updatedComment.parentId,
            canEdit = true,
            canDelete = true
        )
    }

    // 댓글 삭제
    fun deleteComment(postId: Long, commentId: Long, userId: Long) {
        logger.info("댓글 삭제 요청: commentId=$commentId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("존재하지 않는 댓글입니다.")
        }

        if (comment.postId != postId) {
            throw IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.")
        }

        // 게시판 접근 권한 확인
        val clubId = boardService.validateBoardAccess(post.boardId, userId)

        // 삭제 권한 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
        val canDelete = comment.authorId == userId || 
                       (membership?.role == MemberRole.owner || membership?.role == MemberRole.staff)
        
        if (!canDelete) {
            throw IllegalArgumentException("댓글을 삭제할 권한이 없습니다.")
        }

        // 대댓글이 있는 경우 확인
        val hasReplies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId).isNotEmpty()
        if (hasReplies) {
            // 대댓글이 있는 경우 내용만 삭제 표시로 변경 (실제 삭제하지 않음)
            val deletedComment = comment.copy(
                content = "삭제된 댓글입니다.",
                updatedAt = LocalDateTime.now()
            )
            commentRepository.save(deletedComment)
        } else {
            // 대댓글이 없는 경우 완전 삭제
            commentRepository.delete(comment)
        }

        // 게시글의 댓글 수 업데이트
        val commentCount = commentRepository.countByPostId(postId).toInt()
        postRepository.updateCommentCount(postId, commentCount)

        logger.info("댓글 삭제 완료: commentId=$commentId")
    }

    // 댓글 좋아요 토글
    fun toggleCommentLike(postId: Long, commentId: Long, userId: Long): CommentLikeToggleResponse {
        logger.info("댓글 좋아요 토글: commentId=$commentId, userId=$userId")

        val post = postRepository.findById(postId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시글입니다.")
        }

        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("존재하지 않는 댓글입니다.")
        }

        if (comment.postId != postId) {
            throw IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.")
        }

        // 게시판 접근 권한 확인
        boardService.validateBoardAccess(post.boardId, userId)

        val existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)

        val newIsLiked = if (existingLike == null) {
            // 좋아요가 없으면 추가 (게시글 좋아요와 동일한 로직)
            val commentLike = CommentLike(commentId = commentId, userId = userId)
            commentLikeRepository.save(commentLike)
            true
        } else {
            // 좋아요가 있으면 제거 (게시글 좋아요와 동일한 로직)
            commentLikeRepository.delete(existingLike)
            false
        }

        val newLikeCount = commentLikeRepository.countByCommentId(commentId).toInt()

        logger.info("댓글 좋아요 토글 완료: commentId=$commentId, newIsLiked=$newIsLiked, likeCount=$newLikeCount")

        return CommentLikeToggleResponse(
            success = true,
            message = if (newIsLiked) "댓글에 좋아요를 눌렀습니다." else "댓글 좋아요를 취소했습니다.",
            isLiked = newIsLiked,
            likeCount = newLikeCount
        )
    }
}