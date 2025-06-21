package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.BoardService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService
) {

    private val logger = LoggerFactory.getLogger(BoardController::class.java)

    // 동아리 게시판 목록 조회
    @GetMapping("/club/{clubId}")
    fun getBoardsByClub(
        @PathVariable clubId: Long,
        authentication: Authentication
    ): ResponseEntity<BoardListResponse> {
        val userId = authentication.principal as Long
        logger.info("동아리 게시판 목록 조회 요청: clubId=$clubId, userId=$userId")

        return try {
            val response = boardService.getBoardsByClub(clubId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("동아리 게시판 목록 조회 실패: ${e.message}")

            val errorResponse = BoardListResponse(
                success = false,
                message = e.message ?: "게시판 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시판 생성
    @PostMapping
    fun createBoard(
        @Valid @RequestBody request: CreateBoardRequest,
        authentication: Authentication
    ): ResponseEntity<CreateBoardResponse> {
        val userId = authentication.principal as Long
        logger.info("게시판 생성 요청: clubId=${request.clubId}, name=${request.name}, userId=$userId")

        return try {
            val response = boardService.createBoard(request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시판 생성 실패: ${e.message}")

            val errorResponse = CreateBoardResponse(
                success = false,
                message = e.message ?: "게시판을 생성할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시판 수정
    @PutMapping("/{boardId}")
    fun updateBoard(
        @PathVariable boardId: Long,
        @Valid @RequestBody request: UpdateBoardRequest,
        authentication: Authentication
    ): ResponseEntity<UpdateBoardResponse> {
        val userId = authentication.principal as Long
        logger.info("게시판 수정 요청: boardId=$boardId, userId=$userId")

        return try {
            val response = boardService.updateBoard(boardId, request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시판 수정 실패: ${e.message}")

            val errorResponse = UpdateBoardResponse(
                success = false,
                message = e.message ?: "게시판을 수정할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 게시판 삭제 (논리적 삭제)
    @DeleteMapping("/{boardId}")
    fun deleteBoard(
        @PathVariable boardId: Long,
        authentication: Authentication
    ): ResponseEntity<DeleteBoardResponse> {
        val userId = authentication.principal as Long
        logger.info("게시판 삭제 요청: boardId=$boardId, userId=$userId")

        return try {
            val response = boardService.deleteBoard(boardId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("게시판 삭제 실패: ${e.message}")

            val errorResponse = DeleteBoardResponse(
                success = false,
                message = e.message ?: "게시판을 삭제할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}