package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.UserKeyword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserKeywordRepository : JpaRepository<UserKeyword, Int> {

    // 특정 사용자의 키워드 목록 조회
    fun findByUserId(userId: Long): List<UserKeyword>

    // 특정 키워드를 등록한 사용자들 조회
    fun findByKeyword(keyword: String): List<UserKeyword>

    // 텍스트에 포함된 키워드를 등록한 사용자들 조회
    @Query("""
        SELECT uk FROM UserKeyword uk 
        WHERE LOWER(:text) LIKE LOWER(CONCAT('%', uk.keyword, '%'))
    """)
    fun findUsersWithKeywordsInText(@Param("text") text: String): List<UserKeyword>

    // 사용자별 키워드 존재 여부 확인
    fun existsByUserIdAndKeyword(userId: Long, keyword: String): Boolean

    // 사용자의 특정 키워드 삭제
    fun deleteByUserIdAndKeyword(userId: Long, keyword: String): Int
}