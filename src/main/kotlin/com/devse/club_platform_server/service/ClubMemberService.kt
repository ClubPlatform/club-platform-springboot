package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ClubMemberService(
    private val clubMemberRepository: ClubMemberRepository,
    private val clubRepository: ClubRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(ClubMemberService::class.java)

    // 동아리 멤버 목록 조회
    @Transactional(readOnly = true)
    fun getClubMembers(clubId: Long, requestUserId: Long): ClubMemberListResponse {
        // 동아리 존재 확인
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 비활성화된 동아리 체크
        if (!club.isActive) {
            throw IllegalArgumentException("비활성화된 동아리입니다.")
        }

        // 요청자가 해당 동아리 멤버인지 확인
        val requestMembership = clubMemberRepository.findByUserIdAndClubIdAndStatus(requestUserId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버만 멤버 목록을 조회할 수 있습니다.")

        // 활성 멤버 목록 조회
        val members = clubMemberRepository.findByClubIdAndStatusOrderByJoinedAtDesc(clubId, MemberStatus.active)

        // 사용자 정보와 함께 멤버 정보 구성
        val memberInfos = members.mapNotNull { member ->
            val user = userRepository.findByIdOrNull(member.userId)
            user?.let {
                ClubMemberInfo(
                    memberId = member.memberId,
                    userId = member.userId,
                    userName = user.name,
                    userEmail = user.email,
                    university = user.university,
                    department = user.department,
                    major = user.major,
                    studentId = user.studentId,
                    profileImage = user.profileImage,
                    role = member.role,
                    status = member.status,
                    joinedAt = member.joinedAt
                )
            }
        }

        val totalMembers = clubMemberRepository.countByClubIdAndStatus(clubId, MemberStatus.active)

        return ClubMemberListResponse(
            success = true,
            message = "멤버 목록 조회 성공",
            members = memberInfos,
            totalMembers = totalMembers
        )
    }

    // 멤버 역할 변경
    fun updateMemberRole(
        clubId: Long,
        targetUserId: Long,
        request: UpdateMemberRoleRequest,
        requestUserId: Long
    ): UpdateMemberRoleResponse {
        // 동아리 존재 확인
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 요청자 권한 확인
        val requestMembership = clubMemberRepository.findByUserIdAndClubIdAndStatus(requestUserId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        // 대상 멤버 확인
        val targetMembership = clubMemberRepository.findByUserIdAndClubIdAndStatus(targetUserId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("대상 사용자가 동아리 멤버가 아닙니다.")

        // 권한 검증
        validateRoleChangePermission(requestMembership, targetMembership, request.newRole)

        // 소유자 역할 변경 시 특별 처리
        if (request.newRole == MemberRole.owner) {
            // 기존 소유자를 관리자로 변경
            val currentOwner = clubMemberRepository.findByClubIdAndRole(clubId, MemberRole.owner)
            currentOwner?.let {
                val demotedOwner = it.copy(role = MemberRole.staff)
                clubMemberRepository.save(demotedOwner)
                logger.info("기존 소유자 권한 변경: userId=${it.userId}, clubId=$clubId")
            }
        }

        // 멤버 역할 업데이트
        val updatedMember = targetMembership.copy(role = request.newRole)
        clubMemberRepository.save(updatedMember)

        logger.info("멤버 역할 변경 완료: clubId=$clubId, targetUserId=$targetUserId, newRole=${request.newRole}")

        return UpdateMemberRoleResponse(
            success = true,
            message = "멤버 역할이 변경되었습니다.",
            memberId = updatedMember.memberId,
            newRole = request.newRole
        )
    }

    // 멤버 강퇴
    fun removeMember(
        clubId: Long,
        targetUserId: Long,
        request: RemoveMemberRequest,
        requestUserId: Long
    ): RemoveMemberResponse {
        // 동아리 존재 확인
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 비활성화된 동아리 체크
        if (!club.isActive) {
            throw IllegalArgumentException("비활성화된 동아리입니다.")
        }

        // 자기 자신을 강퇴할 수 없음
        if (requestUserId == targetUserId) {
            throw IllegalArgumentException("자기 자신을 강퇴할 수 없습니다. 탈퇴 기능을 사용하세요.")
        }

        // 요청자 권한 확인
        val requestMembership = clubMemberRepository.findByUserIdAndClubIdAndStatus(requestUserId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        // 대상 멤버 확인
        val targetMembership = clubMemberRepository.findByUserIdAndClubIdAndStatus(targetUserId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("대상 사용자가 동아리 멤버가 아닙니다.")

        // 권한 검증
        validateRemovePermission(requestMembership, targetMembership)

        // 멤버 상태를 비활성으로 변경
        val removedMember = targetMembership.copy(status = MemberStatus.inactive)
        clubMemberRepository.save(removedMember)

        logger.info("멤버 강퇴 완료: clubId=$clubId, targetUserId=$targetUserId, reason=${request.reason}")

        return RemoveMemberResponse(
            success = true,
            message = "멤버가 강퇴되었습니다.",
            removedUserId = targetUserId
        )
    }

    // 동아리 탈퇴
    fun leaveClub(clubId: Long, userId: Long): LeaveClubResponse {
        // 동아리 존재 확인
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 비활성화된 동아리 체크
        if (!club.isActive) {
            throw IllegalArgumentException("비활성화된 동아리입니다.")
        }

        // 멤버십 확인
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        // 소유자는 탈퇴할 수 없음 (소유권 이전 필요)
        if (membership.role == MemberRole.owner) {
            val adminMembers = clubMemberRepository.findByClubIdAndRoleAndStatus(
                clubId, MemberRole.staff, MemberStatus.active
            )

            if (adminMembers.isNotEmpty()) {
                throw IllegalArgumentException("소유자는 탈퇴하기 전에 다른 관리자에게 소유권을 이전해야 합니다.")
            } else {
                throw IllegalArgumentException("소유자는 탈퇴할 수 없습니다. 동아리를 삭제하거나 다른 멤버를 관리자로 승격시킨 후 소유권을 이전하세요.")
            }
        }

        // 멤버 상태를 비활성으로 변경
        val leftMember = membership.copy(status = MemberStatus.inactive)
        clubMemberRepository.save(leftMember)

        logger.info("동아리 탈퇴 완료: clubId=$clubId, userId=$userId")

        return LeaveClubResponse(
            success = true,
            message = "동아리에서 탈퇴했습니다.",
            clubId = clubId
        )
    }

    // 내 동아리 멤버십 목록 조회
    @Transactional(readOnly = true)
    fun getMyClubMemberships(userId: Long): MyClubListResponse {
        val memberships = clubMemberRepository.findByUserIdAndStatusOrderByJoinedAtDesc(userId, MemberStatus.active)

        val clubInfos = memberships.mapNotNull { membership ->
            val club = clubRepository.findByIdOrNull(membership.clubId)
            club?.takeIf { it.isActive }?.let {
                val memberCount = clubMemberRepository.countByClubIdAndStatus(club.clubId, MemberStatus.active)

                MyClubMembershipInfo(
                    clubId = club.clubId,
                    clubName = club.name,
                    clubLogoImage = club.logoImage,
                    role = membership.role,
                    status = membership.status,
                    joinedAt = membership.joinedAt,
                    memberCount = memberCount
                )
            }
        }

        return MyClubListResponse(
            success = true,
            message = "내 동아리 멤버십 목록 조회 성공",
            clubs = clubInfos
        )
    }

    // 역할 변경 권한 검증
    private fun validateRoleChangePermission(
        requestMembership: ClubMember,
        targetMembership: ClubMember,
        newRole: MemberRole
    ) {
        when (requestMembership.role) {
            MemberRole.owner -> {
                // 소유자는 모든 역할 변경 가능
                return
            }
            MemberRole.staff -> {
                // 관리자는 일반 멤버만 역할 변경 가능
                if (targetMembership.role != MemberRole.member) {
                    throw IllegalArgumentException("관리자는 일반 멤버의 역할만 변경할 수 있습니다.")
                }
                // 관리자는 소유자로 승격시킬 수 없음
                if (newRole == MemberRole.owner) {
                    throw IllegalArgumentException("관리자는 소유자로 승격시킬 수 없습니다.")
                }
            }
            MemberRole.member -> {
                throw IllegalArgumentException("일반 멤버는 역할을 변경할 권한이 없습니다.")
            }
        }
    }

    // 멤버 강퇴 권한 검증
    private fun validateRemovePermission(
        requestMembership: ClubMember,
        targetMembership: ClubMember
    ) {
        when (requestMembership.role) {
            MemberRole.owner -> {
                // 소유자는 모든 멤버 강퇴 가능
                return
            }
            MemberRole.staff -> {
                // 관리자는 일반 멤버만 강퇴 가능
                if (targetMembership.role != MemberRole.member) {
                    throw IllegalArgumentException("관리자는 일반 멤버만 강퇴할 수 있습니다.")
                }
            }
            MemberRole.member -> {
                throw IllegalArgumentException("일반 멤버는 다른 멤버를 강퇴할 권한이 없습니다.")
            }
        }
    }
}