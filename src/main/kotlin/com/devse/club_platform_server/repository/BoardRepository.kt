package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Board
import com.devse.club_platform_server.domain.BoardType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BoardRepository : JpaRepository<Board, Long> {

    // 특정 동아리의 활성 게시판 목록 조회
    fun findByClubIdAndIsActiveTrueOrderByCreatedAtAsc(clubId: Long): List<Board>

    // 특정 동아리의 특정 타입 게시판 조회
    fun findByClubIdAndTypeAndIsActiveTrue(clubId: Long, type: BoardType): List<Board>

    // 게시판 존재 여부 확인
    fun existsByBoardIdAndIsActiveTrue(boardId: Long): Boolean

    // 특정 동아리의 게시판 이름 중복 확인 (활성 게시판만)
    fun existsByClubIdAndNameAndIsActiveTrue(clubId: Long, name: String): Boolean

    // 게시판 이름 중복 확인 (수정 시 - 자신 제외)
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END 
        FROM Board b 
        WHERE b.clubId = :clubId 
        AND b.name = :name 
        AND b.isActive = true 
        AND b.boardId != :excludeBoardId
    """)
    fun existsByClubIdAndNameAndIsActiveTrueExcludingBoard(
        @Param("clubId") clubId: Long,
        @Param("name") name: String,
        @Param("excludeBoardId") excludeBoardId: Long
    ): Boolean

    // 특정 동아리의 모든 게시판 조회 (비활성 포함)
    fun findByClubIdOrderByCreatedAtAsc(clubId: Long): List<Board>

    // 게시판 ID와 동아리 ID로 조회 (권한 확인용)
    fun findByBoardIdAndClubId(boardId: Long, clubId: Long): Board?

    // 활성 게시판만 조회
    @Query("SELECT b FROM Board b WHERE b.boardId = :boardId AND b.isActive = true")
    fun findActiveBoardById(@Param("boardId") boardId: Long): Board?

    // 특정 동아리의 특정 타입 게시판 개수 확인
    fun countByClubIdAndTypeAndIsActiveTrue(clubId: Long, type: BoardType): Long
}