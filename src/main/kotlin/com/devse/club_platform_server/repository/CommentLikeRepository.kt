package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.CommentLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentLikeRepository : JpaRepository<CommentLike, Long> {

    // 특정 사용자가 특정 댓글에 좋아요를 눌렀는지 확인
    fun findByCommentIdAndUserId(commentId: Long, userId: Long): CommentLike?

    // 특정 사용자가 특정 댓글에 좋아요를 눌렀는지 여부
    fun existsByCommentIdAndUserId(commentId: Long, userId: Long): Boolean

    // 특정 댓글의 좋아요 수 카운트
    fun countByCommentId(commentId: Long): Long

    // 특정 사용자의 특정 댓글 좋아요 삭제
    fun deleteByCommentIdAndUserId(commentId: Long, userId: Long)
}