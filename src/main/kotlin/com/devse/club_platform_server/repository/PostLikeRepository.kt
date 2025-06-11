package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.PostLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository : JpaRepository<PostLike, Long> {

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    fun findByPostIdAndUserId(postId: Long, userId: Long): PostLike?

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 여부
    fun existsByPostIdAndUserId(postId: Long, userId: Long): Boolean

    // 특정 게시글의 좋아요 수 카운트
    fun countByPostId(postId: Long): Long

    // 특정 사용자가 좋아요한 게시글 목록
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<PostLike>

    // 특정 게시글에 좋아요한 사용자 목록
    fun findByPostIdOrderByCreatedAtDesc(postId: Long): List<PostLike>

    // 특정 사용자의 특정 게시글 좋아요 삭제
    fun deleteByPostIdAndUserId(postId: Long, userId: Long)
}