package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.BoardRepository
import com.devse.club_platform_server.repository.ClubMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/*
동아리 게시판 관리 서비스
- 동아리별 게시판 목록 조회
- 게시판 생성, 수정, 삭제 기능
- 게시판 접근 권한 관리
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
                name = board.name,
                description = board.description,
                isActive = board.isActive,
                createdAt = board.createdAt,
                updatedAt = board.updatedAt
            )
        }

        logger.info("동아리 게시판 목록 조회 완료: clubId=$clubId, 게시판수=${boardInfos.size}")

        return BoardListResponse(
            success = true,
            message = "게시판 목록 조회 성공",
            boards = boardInfos
        )
    }

    // 게시판 생성
    @Transactional
    fun createBoard(request: CreateBoardRequest, userId: Long): CreateBoardResponse {
        logger.info("게시판 생성 시작: clubId=${request.clubId}, name=${request.name}, userId=$userId")

        // 동아리 관리 권한 확인
        validateAdminPermission(request.clubId, userId)

        // 게시판 이름 중복 확인
        if (boardRepository.existsByClubIdAndNameAndIsActiveTrue(request.clubId, request.name)) {
            throw IllegalArgumentException("해당 동아리에 같은 이름의 게시판이 이미 존재합니다.")
        }

        // BoardType enum 변환
        val boardType = try {
            BoardType.valueOf(request.type)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("유효하지 않은 게시판 유형입니다: ${request.type}")
        }

        // 게시판 생성
        val board = Board(
            clubId = request.clubId,
            name = request.name,
            type = boardType,
            description = request.description,
            isActive = true,
            createdAt = LocalDateTime.now()
        )

        val savedBoard = boardRepository.save(board)

        val boardInfo = BoardInfo(
            boardId = savedBoard.boardId,
            type = savedBoard.type.name,
            name = savedBoard.name,
            description = savedBoard.description,
            isActive = savedBoard.isActive,
            createdAt = savedBoard.createdAt,
            updatedAt = savedBoard.updatedAt
        )

        logger.info("게시판 생성 완료: boardId=${savedBoard.boardId}")

        return CreateBoardResponse(
            success = true,
            message = "게시판이 성공적으로 생성되었습니다.",
            boardId = savedBoard.boardId,
            board = boardInfo
        )
    }

    // 게시판 수정
    @Transactional
    fun updateBoard(boardId: Long, request: UpdateBoardRequest, userId: Long): UpdateBoardResponse {
        logger.info("게시판 수정 시작: boardId=$boardId, userId=$userId")

        // 게시판 존재 확인
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw IllegalArgumentException("존재하지 않는 게시판입니다.")

        if (!board.isActive) {
            throw IllegalArgumentException("비활성화된 게시판은 수정할 수 없습니다.")
        }

        // 동아리 관리 권한 확인
        validateAdminPermission(board.clubId, userId)

        // 게시판 이름 중복 확인 (변경하는 경우)
        if (request.name != null && request.name != board.name) {
            if (boardRepository.existsByClubIdAndNameAndIsActiveTrueExcludingBoard(
                    board.clubId, request.name, boardId
                )) {
                throw IllegalArgumentException("해당 동아리에 같은 이름의 게시판이 이미 존재합니다.")
            }
        }

        // 게시판 정보 업데이트
        val updatedBoard = board.copy(
            name = request.name ?: board.name,
            description = request.description ?: board.description,
            isActive = request.isActive ?: board.isActive,
            updatedAt = LocalDateTime.now()
        )

        val savedBoard = boardRepository.save(updatedBoard)

        val boardInfo = BoardInfo(
            boardId = savedBoard.boardId,
            type = savedBoard.type.name,
            name = savedBoard.name,
            description = savedBoard.description,
            isActive = savedBoard.isActive,
            createdAt = savedBoard.createdAt,
            updatedAt = savedBoard.updatedAt
        )

        logger.info("게시판 수정 완료: boardId=$boardId")

        return UpdateBoardResponse(
            success = true,
            message = "게시판이 성공적으로 수정되었습니다.",
            board = boardInfo
        )
    }

    // 게시판 삭제 (논리적 삭제)
    @Transactional
    fun deleteBoard(boardId: Long, userId: Long): DeleteBoardResponse {
        logger.info("게시판 삭제 시작: boardId=$boardId, userId=$userId")

        // 게시판 존재 확인
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw IllegalArgumentException("존재하지 않는 게시판입니다.")

        if (!board.isActive) {
            throw IllegalArgumentException("이미 삭제된 게시판입니다.")
        }

        // 동아리 관리 권한 확인
        validateAdminPermission(board.clubId, userId)

        // 논리적 삭제 수행
        val deletedBoard = board.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )

        boardRepository.save(deletedBoard)

        logger.info("게시판 삭제 완료: boardId=$boardId")

        return DeleteBoardResponse(
            success = true,
            message = "게시판이 성공적으로 삭제되었습니다."
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

    // 동아리 관리 권한 확인 (owner 또는 staff만 게시판 관리 가능)
    private fun validateAdminPermission(clubId: Long, userId: Long) {
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("해당 동아리의 멤버가 아닙니다.")

        if (membership.role != MemberRole.owner && membership.role != MemberRole.staff) {
            throw IllegalArgumentException("게시판을 관리할 권한이 없습니다. 동아리 관리자 이상의 권한이 필요합니다.")
        }
    }

    // 기본 게시판 생성 (동아리 생성 시 호출)
    @Transactional
    fun createDefaultBoards(clubId: Long) {
        logger.info("기본 게시판 생성 시작: clubId=$clubId")

        val defaultBoards = listOf(
            Board(
                clubId = clubId,
                name = "공지사항",
                type = BoardType.notice,
                description = "동아리 공지사항을 확인하세요",
                isActive = true,
                createdAt = LocalDateTime.now()
            ),
            Board(
                clubId = clubId,
                name = "자유게시판",
                type = BoardType.general,
                description = "자유롭게 소통해보세요",
                isActive = true,
                createdAt = LocalDateTime.now()
            ),
            Board(
                clubId = clubId,
                name = "정보공유",
                type = BoardType.tips,
                description = "유용한 정보를 공유해주세요",
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )

        boardRepository.saveAll(defaultBoards)

        logger.info("기본 게시판 생성 완료: clubId=$clubId, 생성된 게시판 수=${defaultBoards.size}")
    }
}