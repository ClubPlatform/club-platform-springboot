package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.MemberStatus
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.BoardRepository
import com.devse.club_platform_server.repository.ClubMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/*
동아리 게시판 관리 서비스
- 동아리별 게시판 목록 조회
 */

@Service
@Transactional(readOnly = true)
class BoardService(
    private val boardRepository: BoardRepository,
    private val clubMemberRepository: ClubMemberRepository
) {

    private val logger = LoggerFactory.getLogger(BoardService::class.java)

    // 동아리 게시판 목록 조회
    fun getBoardsByClub(clubId: Long, userId: Long): BoardListResponse {
        logger.info("동아리 게시판 목록 조회: clubId=$clubId, userId=$userId")

        // 사용자가 해당 동아리 멤버인지 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
        if (membership == null) {
            throw IllegalArgumentException("해당 동아리의 멤버만 게시판에 접근할 수 있습니다.")
        }

        // 해당 동아리의 활성 게시판 목록 조회
        val boards = boardRepository.findByClubIdAndIsActiveTrueOrderByCreatedAtAsc(clubId)

        val boardInfos = boards.map { board ->
            BoardInfo(
                boardId = board.boardId,
                type = board.type.name,
                name = board.name
            )
        }

        logger.info("동아리 게시판 목록 조회 완료: clubId=$clubId, 게시판수=${boardInfos.size}")

        return BoardListResponse(
            success = true,
            message = "게시판 목록 조회 성공",
            boards = boardInfos
        )
    }

    // 게시판 접근 권한 확인
    fun validateBoardAccess(boardId: Long, userId: Long): Long {
        val board = boardRepository.findById(boardId).orElseThrow {
            IllegalArgumentException("존재하지 않는 게시판입니다.")
        }

        if (!board.isActive) {
            throw IllegalArgumentException("비활성화된 게시판입니다.")
        }

        // 사용자가 해당 동아리 멤버인지 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, board.clubId, MemberStatus.active)
        if (membership == null) {
            throw IllegalArgumentException("해당 동아리의 멤버만 게시판에 접근할 수 있습니다.")
        }

        return board.clubId
    }
}