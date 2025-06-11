package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.BoardService
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
}