package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Board
import com.devse.club_platform_server.domain.BoardType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardRepository : JpaRepository<Board, Long> {

    // 특정 동아리의 활성 게시판 목록 조회
    fun findByClubIdAndIsActiveTrueOrderByCreatedAtAsc(clubId: Long): List<Board>

    // 특정 동아리의 특정 타입 게시판 조회
    fun findByClubIdAndTypeAndIsActiveTrue(clubId: Long, type: BoardType): List<Board>

    // 게시판 존재 여부 확인
    fun existsByBoardIdAndIsActiveTrue(boardId: Long): Boolean
}