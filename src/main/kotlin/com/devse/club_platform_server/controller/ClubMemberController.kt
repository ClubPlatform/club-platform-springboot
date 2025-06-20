package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.ClubMemberService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clubs")
class ClubMemberController(
    private val clubMemberService: ClubMemberService
) {

    private val logger = LoggerFactory.getLogger(ClubMemberController::class.java)

    // 동아리 멤버 목록 조회
    @GetMapping("/{clubId}/members")
    fun getClubMembers(
        @PathVariable clubId: Long,
        authentication: Authentication
    ): ResponseEntity<ClubMemberListResponse> {
        val userId = authentication.principal as Long
        logger.info("동아리 멤버 목록 조회: clubId=$clubId, 요청자: $userId")

        return try {
            val response = clubMemberService.getClubMembers(clubId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("동아리 멤버 목록 조회 실패: ${e.message}")

            val errorResponse = ClubMemberListResponse(
                success = false,
                message = e.message ?: "멤버 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 멤버 역할 변경
    @PutMapping("/{clubId}/members/{targetUserId}/role")
    fun updateMemberRole(
        @PathVariable clubId: Long,
        @PathVariable targetUserId: Long,
        @Valid @RequestBody request: UpdateMemberRoleRequest,
        authentication: Authentication
    ): ResponseEntity<UpdateMemberRoleResponse> {
        val requestUserId = authentication.principal as Long
        logger.info("멤버 역할 변경 요청: clubId=$clubId, targetUser=$targetUserId, newRole=${request.newRole}, 요청자: $requestUserId")

        return try {
            val response = clubMemberService.updateMemberRole(clubId, targetUserId, request, requestUserId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("멤버 역할 변경 실패: ${e.message}")

            val errorResponse = UpdateMemberRoleResponse(
                success = false,
                message = e.message ?: "멤버 역할을 변경할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 멤버 강퇴 (멤버 비활성화)
    @PutMapping("/{clubId}/members/{targetUserId}/kick")
    fun kickMember(
        @PathVariable clubId: Long,
        @PathVariable targetUserId: Long,
        authentication: Authentication
    ): ResponseEntity<RemoveMemberResponse> {
        val requestUserId = authentication.principal as Long
        logger.info("멤버 강퇴 요청: clubId=$clubId, targetUser=$targetUserId, 요청자: $requestUserId")

        return try {
            val response = clubMemberService.removeMember(
                clubId,
                targetUserId,
                requestUserId
            )
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("멤버 강퇴 실패: ${e.message}")

            val errorResponse = RemoveMemberResponse(
                success = false,
                message = e.message ?: "멤버를 강퇴할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 동아리 탈퇴 (멤버 비활성화)
    @PutMapping("/{clubId}/leave")
    fun leaveClub(
        @PathVariable clubId: Long,
        authentication: Authentication
    ): ResponseEntity<LeaveClubResponse> {
        val userId = authentication.principal as Long
        logger.info("동아리 탈퇴 요청: clubId=$clubId, userId=$userId")

        return try {
            val response = clubMemberService.leaveClub(clubId, userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("동아리 탈퇴 실패: ${e.message}")

            val errorResponse = LeaveClubResponse(
                success = false,
                message = e.message ?: "동아리를 탈퇴할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    // 내 동아리 멤버십 목록 조회
    @GetMapping("/memberships/my")
    fun getMyClubMemberships(authentication: Authentication): ResponseEntity<MyClubListResponse> {
        val userId = authentication.principal as Long
        logger.info("내 동아리 멤버십 목록 조회: userId=$userId")

        return try {
            val response = clubMemberService.getMyClubMemberships(userId)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("내 동아리 멤버십 목록 조회 실패: ${e.message}")

            val errorResponse = MyClubListResponse(
                success = false,
                message = e.message ?: "내 동아리 멤버십 목록을 조회할 수 없습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}