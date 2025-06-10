package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.ClubMember
import com.devse.club_platform_server.domain.MemberRole
import com.devse.club_platform_server.domain.MemberStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClubMemberRepository : JpaRepository<ClubMember, Long> {

    // 특정 동아리의 활성 멤버 조회
    fun findByClubIdAndStatusOrderByJoinedAtDesc(
        clubId: Long,
        status: MemberStatus
    ): List<ClubMember>

    // 특정 사용자의 동아리 멤버십 조회
    fun findByUserIdAndStatusOrderByJoinedAtDesc(
        userId: Long,
        status: MemberStatus
    ): List<ClubMember>

    // 특정 사용자가 특정 동아리의 활성화된 멤버인지 확인
    fun findByUserIdAndClubIdAndStatus(
        userId: Long,
        clubId: Long,
        status: MemberStatus
    ): ClubMember?

    // 특정 사용자가 특정 동아리의 멤버인지 확인 (상태 무관)
    fun findByUserIdAndClubId(userId: Long, clubId: Long): ClubMember?

    // 동아리의 특정 역할 멤버 조회
    fun findByClubIdAndRoleAndStatus(
        clubId: Long,
        role: MemberRole,
        status: MemberStatus
    ): List<ClubMember>

    // 동아리 멤버 수 카운트
    fun countByClubIdAndStatus(clubId: Long, status: MemberStatus): Long

    // 동아리 소유자 조회
    fun findByClubIdAndRole(clubId: Long, role: MemberRole): ClubMember?

    // 동아리 관리자급 이상 멤버 조회 (owner, staff)
    @Query("""
        SELECT cm FROM ClubMember cm 
        WHERE cm.clubId = :clubId 
        AND cm.status = 'active' 
        AND cm.role IN ('owner', 'staff')
        ORDER BY cm.role DESC, cm.joinedAt ASC
    """)
    fun findAdminMembers(@Param("clubId") clubId: Long): List<ClubMember>

    // 동아리의 모든 활성 멤버 조회
    @Query("""
        SELECT cm FROM ClubMember cm 
        WHERE cm.clubId = :clubId AND cm.status = 'active'
    """)
    fun findActiveClubMembers(@Param("clubId") clubId: Long): List<ClubMember>
}