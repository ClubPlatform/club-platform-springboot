package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Scrap
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScrapRepository : JpaRepository<Scrap, Long> {

    // 특정 사용자가 특정 게시글을 스크랩했는지 확인
    fun findByUserIdAndPostId(userId: Long, postId: Long): Scrap?

    // 특정 사용자가 특정 게시글을 스크랩했는지 여부
    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean

    // 특정 사용자의 스크랩 목록
    fun findByUserIdOrderByScrappedAtDesc(userId: Long): List<Scrap>

    // 특정 게시글의 스크랩 수 카운트
    fun countByPostId(postId: Long): Long

    // 특정 사용자의 특정 게시글 스크랩 삭제
    fun deleteByUserIdAndPostId(userId: Long, postId: Long)
}