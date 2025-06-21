package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ScheduleRepository : JpaRepository<Schedule, Long> {

    // 특정 동아리의 일정 목록 조회
    fun findByClubIdOrderByStartDateAsc(clubId: Long): List<Schedule>

    // 특정 기간의 일정 조회
    fun findByClubIdAndStartDateBetweenOrderByStartDateAsc(
        clubId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Schedule>

    // 오늘 시작하는 일정 조회 (리마인더 알림용)
    @Query("""
        SELECT s FROM Schedule s 
        WHERE DATE(s.startDate) = DATE(:today)
        ORDER BY s.startDate ASC
    """)
    fun findTodaySchedules(@Param("today") today: LocalDateTime): List<Schedule>

    // 특정 사용자가 속한 동아리들의 오늘 일정 조회
    @Query("""
        SELECT s FROM Schedule s 
        JOIN ClubMember cm ON s.clubId = cm.clubId 
        WHERE cm.userId = :userId 
        AND cm.status = 'active'
        AND DATE(s.startDate) = DATE(:today)
        ORDER BY s.startDate ASC
    """)
    fun findTodaySchedulesForUser(
        @Param("userId") userId: Long,
        @Param("today") today: LocalDateTime
    ): List<Schedule>
}