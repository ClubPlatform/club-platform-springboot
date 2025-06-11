package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PostRepository : JpaRepository<Post, Long> {

    // 특정 게시판의 게시글 목록 조회 (공지사항을 맨 위에)
    @Query("""
        SELECT p FROM Post p 
        WHERE p.boardId = :boardId 
        ORDER BY p.isNotice DESC, p.createdAt DESC
    """)
    fun findByBoardIdOrderByNoticeAndCreatedAt(@Param("boardId") boardId: Long): List<Post>

    // 내가 작성한 게시글 조회
    fun findByAuthorIdOrderByCreatedAtDesc(authorId: Long): List<Post>

    // 내가 댓글을 단 게시글 조회
    @Query("""
        SELECT DISTINCT p FROM Post p 
        JOIN Comment c ON p.postId = c.postId 
        WHERE c.authorId = :userId 
        ORDER BY p.createdAt DESC
    """)
    fun findPostsWithMyComments(@Param("userId") userId: Long): List<Post>

    // 내가 스크랩한 게시글 조회
    @Query("""
        SELECT p FROM Post p 
        JOIN Scrap s ON p.postId = s.postId 
        WHERE s.userId = :userId 
        ORDER BY s.scrappedAt DESC
    """)
    fun findMyScrapedPosts(@Param("userId") userId: Long): List<Post>

    // HOT 게시글 조회 (일주일 내, 총 상호작용 20개 이상)
    @Query("""
        SELECT p FROM Post p 
        WHERE p.boardId = :boardId 
        AND p.createdAt >= :weekAgo 
        AND (p.likeCount + p.viewCount + p.commentCount) >= 20 
        ORDER BY (p.likeCount + p.viewCount + p.commentCount) DESC, p.createdAt DESC
    """)
    fun findHotPosts(
        @Param("boardId") boardId: Long,
        @Param("weekAgo") weekAgo: LocalDateTime
    ): List<Post>

    // BEST 게시글 조회 (한달 내, 총 상호작용 50개 이상)
    @Query("""
        SELECT p FROM Post p 
        WHERE p.boardId = :boardId 
        AND p.createdAt >= :monthAgo 
        AND (p.likeCount + p.viewCount + p.commentCount) >= 50 
        ORDER BY (p.likeCount + p.viewCount + p.commentCount) DESC, p.createdAt DESC
    """)
    fun findBestPosts(
        @Param("boardId") boardId: Long,
        @Param("monthAgo") monthAgo: LocalDateTime
    ): List<Post>

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    fun incrementViewCount(@Param("postId") postId: Long)

    // 좋아요 수 업데이트
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = :likeCount WHERE p.postId = :postId")
    fun updateLikeCount(@Param("postId") postId: Long, @Param("likeCount") likeCount: Int)

    // 댓글 수 업데이트
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = :commentCount WHERE p.postId = :postId")
    fun updateCommentCount(@Param("postId") postId: Long, @Param("commentCount") commentCount: Int)
}