package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.ClubService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clubs")
class ClubController(
    private val clubService: ClubService
) {

    private val logger = LoggerFactory.getLogger(ClubController::class.java)

    // 동아리 생성
    @PostMapping
    fun createClub(
        @Valid @RequestBody request: CreateClubRequest,
        authentication: Authentication
    ): ResponseEntity<CreateClubResponse> {
        val userId = authentication.principal as Long
        logger.info("동아리 생성 요청: ${request.name}, 생성자: $userId")

        return try {
            val response = clubService.createClub(request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("동아리 생성 실패", e)

            val errorResponse = CreateClubResponse(
                success = false,
                message = e.message ?: "동아리 생성에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 동아리 상세 조회
    @GetMapping("/{clubId}")
    fun getClub(
        @PathVariable clubId: Long,
        authentication: Authentication?
    ): ResponseEntity<ApiResponse<ClubInfo>> {
        val userId = authentication?.principal as? Long
        logger.info("동아리 조회 요청: clubId=$clubId, 요청자: $userId")

        return try {
            val clubInfo = clubService.getClub(clubId, userId)
            ResponseEntity.ok(ApiResponse.success("동아리 조회 성공", clubInfo))

        } catch (e: Exception) {
            logger.error("동아리 조회 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "동아리 정보를 조회할 수 없습니다."))
        }
    }

    // 공개 동아리 목록 조회 (검색 포함)
    @GetMapping
    fun getPublicClubs(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) category: String?,
        authentication: Authentication?
    ): ResponseEntity<ClubListResponse> {
        val userId = authentication?.principal as? Long
        logger.info("공개 동아리 목록 조회: keyword=$keyword, category=$category")

        return try {
            val response = clubService.getPublicClubs(keyword, category, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("공개 동아리 목록 조회 실패", e)

            val errorResponse = ClubListResponse(
                success = false,
                message = e.message ?: "동아리 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 내가 속한 동아리 목록 조회
    @GetMapping("/my")
    fun getMyClubs(authentication: Authentication): ResponseEntity<ApiResponse<List<ClubInfo>>> {
        val userId = authentication.principal as Long
        logger.info("내 동아리 목록 조회: userId=$userId")

        return try {
            val clubs = clubService.getMyClubs(userId)
            ResponseEntity.ok(ApiResponse.success("내 동아리 목록 조회 성공", clubs))

        } catch (e: Exception) {
            logger.error("내 동아리 목록 조회 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "내 동아리 목록을 조회할 수 없습니다."))
        }
    }

    // 동아리 수정
    @PutMapping("/{clubId}")
    fun updateClub(
        @PathVariable clubId: Long,
        @Valid @RequestBody request: UpdateClubRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ClubInfo>> {
        val userId = authentication.principal as Long
        logger.info("동아리 수정 요청: clubId=$clubId, 수정자: $userId")

        return try {
            val updatedClub = clubService.updateClub(clubId, request, userId)
            ResponseEntity.ok(ApiResponse.success("동아리 정보가 수정되었습니다.", updatedClub))

        } catch (e: Exception) {
            logger.error("동아리 수정 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "동아리 정보를 수정할 수 없습니다."))
        }
    }

    // 동아리 삭제 (논리적 삭제)
    @PutMapping("/{clubId}/deactivate")
    fun deactivateClub(
        @PathVariable clubId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("동아리 비활성화 요청: clubId=$clubId, 요청자: $userId")

        return try {
            clubService.deactivateClub(clubId, userId)
            ResponseEntity.ok(ApiResponse.success("동아리가 비활성화되었습니다.", ""))

        } catch (e: Exception) {
            logger.error("동아리 비활성화 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "동아리를 비활성화할 수 없습니다."))
        }
    }

    // 초대 링크 생성
    @PostMapping("/{clubId}/invite")
    fun generateInviteLink(
        @PathVariable clubId: Long,
        authentication: Authentication
    ): ResponseEntity<InviteLinkResponse> {
        val userId = authentication.principal as Long
        logger.info("초대 링크 생성 요청: clubId=$clubId, 요청자: $userId")

        return try {
            val response = clubService.generateInviteLink(clubId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("초대 링크 생성 실패: ${e.message}")

            val errorResponse = InviteLinkResponse(
                success = false,
                message = e.message ?: "초대 링크를 생성할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 초대 코드로 동아리 가입
    @PostMapping("/join")
    fun joinClubByInviteCode(
        @Valid @RequestBody request: JoinClubRequest,
        authentication: Authentication
    ): ResponseEntity<JoinClubResponse> {
        val userId = authentication.principal as Long
        logger.info("초대 코드로 동아리 가입 요청: code=${request.inviteCode}, userId=$userId")

        return try {
            val response = clubService.joinClubByInviteCode(request, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("동아리 가입 실패: ${e.message}")

            val errorResponse = JoinClubResponse(
                success = false,
                message = e.message ?: "동아리 가입에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}