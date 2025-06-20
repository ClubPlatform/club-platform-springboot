package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Club
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClubRepository : JpaRepository<Club, Long> {

    // 공개 동아리 조회
    fun findByVisibilityOrderByCreatedAtDesc(visibility: String): List<Club>

    // 카테고리별 공개 동아리 조회
    fun findByVisibilityAndCategoryOrderByCreatedAtDesc(
        visibility: String,
        category: Int  // ClubCategory -> Int (category_id)
    ): List<Club>

    // 동아리명으로 검색 (공개 동아리만)
    fun findByVisibilityAndNameContainingIgnoreCaseOrderByCreatedAtDesc(
        visibility: String,
        name: String
    ): List<Club>

    // 특정 사용자가 생성한 동아리 조회
    fun findByCreatedByOrderByCreatedAtDesc(createdBy: Long): List<Club>

    // 동아리명 중복 검사
    fun existsByName(name: String): Boolean

    // 가입코드로 동아리 조회
    fun findByInviteCode(inviteCode: String): Club?

    // 가입코드 중복 검사
    fun existsByInviteCode(inviteCode: String): Boolean

    // 내가 속한 동아리 조회
    @Query("""
        SELECT c FROM Club c 
        JOIN ClubMember cm ON c.clubId = cm.clubId 
        WHERE cm.userId = :userId AND cm.status = 'active'
        ORDER BY c.createdAt DESC
    """)
    fun findMyClubs(@Param("userId") userId: Long): List<Club>

    // 추가: 복합 검색 쿼리 (키워드 + 카테고리 + 공개범위)
    @Query("""
        SELECT c FROM Club c 
        WHERE (:visibility IS NULL OR c.visibility = :visibility)
        AND (:category IS NULL OR c.category = :category)
        AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND c.isActive = true
        ORDER BY c.createdAt DESC
    """)
    fun findClubsWithFilters(
        @Param("visibility") visibility: String?,
        @Param("category") category: Int?,
        @Param("keyword") keyword: String?
    ): List<Club>

    // 활성화된 동아리만 조회
    @Query("""
        SELECT c FROM Club c 
        WHERE c.isActive = true 
        AND (:visibility IS NULL OR c.visibility = :visibility)
        AND (:category IS NULL OR c.category = :category)
        AND (:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%)
        ORDER BY c.createdAt DESC
    """)
    fun findActiveClubsWithFilters(
        @Param("visibility") visibility: String?,
        @Param("category") category: Long?,
        @Param("keyword") keyword: String?
    ): List<Club>

    // 특정 동아리 조회 (활성화된 것만)
    @Query("SELECT c FROM Club c WHERE c.clubId = :clubId AND c.isActive = true")
    fun findActiveClubById(@Param("clubId") clubId: Long): Club?

    // 활성화된 동아리 중 가입코드로 조회 (대소문자 무시)
    @Query("SELECT c FROM Club c WHERE UPPER(c.inviteCode) = UPPER(:inviteCode) AND c.isActive = true")
    fun findActiveClubByInviteCode(@Param("inviteCode") inviteCode: String): Club?
}