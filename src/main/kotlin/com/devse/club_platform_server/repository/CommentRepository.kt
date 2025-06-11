package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 목록 조회 (대댓글 포함, 계층 구조)
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.postId = :postId 
        ORDER BY 
            CASE WHEN c.parentId IS NULL THEN c.commentId ELSE c.parentId END,
            CASE WHEN c.parentId IS NULL THEN 0 ELSE 1 END,
            c.createdAt ASC
    """)
    fun findByPostIdOrderByHierarchy(@Param("postId") postId: Long): List<Comment>

    // 특정 게시글의 댓글 수 카운트
    fun countByPostId(postId: Long): Long

    // 부모 댓글의 대댓글 목록 조회
    fun findByParentIdOrderByCreatedAtAsc(parentId: Long): List<Comment>

    // 특정 사용자가 작성한 댓글 목록
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: Long): List<Comment>
}