package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import com.devse.club_platform_server.util.InviteCodeGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ClubService(
    private val clubRepository: ClubRepository,
    private val clubMemberRepository: ClubMemberRepository,
    private val clubCategoryRepository: ClubCategoryRepository,
    private val userRepository: UserRepository,
    private val inviteCodeGenerator: InviteCodeGenerator
) {

    private val logger = LoggerFactory.getLogger(ClubService::class.java)
    private val uploadPath = "uploads/clubs/"

    @Value("\${app.base-url:http://localhost:3000}")
    private lateinit var baseUrl: String

    @Value("\${app.frontend-url:http://localhost:3000}")
    private lateinit var frontendUrl: String

    @Value("\${app.invite.deep-link-scheme:clubapp}")
    private lateinit var deepLinkScheme: String

    // 동아리 생성
    fun createClub(request: CreateClubRequest, userId: Long): CreateClubResponse {
        logger.info("동아리 생성 시작: ${request.name}, 생성자: $userId")

        // 동아리명 중복 검사
        if (clubRepository.existsByName(request.name)) {
            throw IllegalArgumentException("이미 존재하는 동아리명입니다.")
        }

        // 사용자 존재 확인
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

        // 카테고리 확인 및 조회
        val category = clubCategoryRepository.findByNameIgnoreCase(request.category)
            .orElseThrow { IllegalArgumentException("존재하지 않는 카테고리입니다: ${request.category}") }

        // 로고 이미지 처리
        val logoImagePath = request.logoImage?.let {
            saveClubLogo(it)
        }

        // 동아리 생성
        val club = Club(
            name = request.name,
            description = request.description,
            category = category.categoryId,
            organization = request.organization,
            logoImage = logoImagePath,
            meetingPlace = request.meetingPlace,
            meetingTime = request.meetingTime,
            fee = request.fee,
            joinMethod = request.joinMethod,
            visibility = request.visibility,
            createdBy = userId,
            createdAt = LocalDateTime.now()
        )

        val savedClub = clubRepository.save(club)

        // 생성자를 동아리의 소유자로 자동 등록
        val ownerMember = ClubMember(
            userId = userId,
            clubId = savedClub.clubId,
            role = MemberRole.owner,
            status = MemberStatus.active,
            joinedAt = LocalDateTime.now()
        )

        clubMemberRepository.save(ownerMember)

        // 초대 코드 생성
        val inviteCode = inviteCodeGenerator.generateInviteCode(savedClub.clubId)

        logger.info("동아리 생성 완료: clubId=${savedClub.clubId}")

        return CreateClubResponse(
            success = true,
            message = "동아리가 성공적으로 생성되었습니다.",
            clubId = savedClub.clubId,
            inviteCode = inviteCode
        )
    }

    // 동아리 상세 조회
    @Transactional(readOnly = true)
    fun getClub(clubId: Long, userId: Long?): ClubInfo {
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 비활성화된 동아리 확인
        if (!club.isActive) {
            throw IllegalArgumentException("비활성화된 동아리입니다.")
        }

        // 멤버 수 조회
        val memberCount = clubMemberRepository.countByClubIdAndStatus(clubId, MemberStatus.active)

        // 요청자의 멤버십 정보 (로그인한 경우만)
        val membership = userId?.let {
            clubMemberRepository.findByUserIdAndClubIdAndStatus(it, clubId, MemberStatus.active)
        }

        // 카테고리 정보 조회
        val categoryInfo = club.category?.let {
            clubCategoryRepository.findByIdOrNull(it)
        }

        return ClubInfo(
            clubId = club.clubId,
            name = club.name,
            description = club.description,
            category = categoryInfo,
            organization = club.organization,
            logoImage = club.logoImage,
            meetingPlace = club.meetingPlace,
            meetingTime = club.meetingTime,
            fee = club.fee,
            joinMethod = club.joinMethod,
            visibility = club.visibility,
            createdBy = club.createdBy,
            createdAt = club.createdAt,
            updatedAt = club.updatedAt,
            memberCount = memberCount,
            myRole = membership?.role,
            isJoined = membership != null
        )
    }

    // 공개 동아리 목록 조회
    @Transactional(readOnly = true)
    fun getPublicClubs(keyword: String?, category: String?, userId: Long?): ClubListResponse {
        val categoryId = category?.let {
            clubCategoryRepository.findByNameIgnoreCase(it).orElse(null)?.categoryId
        }

        // 기존 메소드 사용하고 isActive = true인 것만 필터링
        val allClubs = clubRepository.findClubsWithFilters(
            visibility = "public",
            category = categoryId,
            keyword = keyword
        )

        // 활성화된 동아리만 필터링
        val clubs = allClubs.filter { it.isActive }

        val clubInfos = clubs.map { club ->
            val memberCount = clubMemberRepository.countByClubIdAndStatus(club.clubId, MemberStatus.active)
            val membership = userId?.let {
                clubMemberRepository.findByUserIdAndClubIdAndStatus(it, club.clubId, MemberStatus.active)
            }
            val categoryInfo = club.category?.let {
                clubCategoryRepository.findByIdOrNull(it)
            }

            ClubInfo(
                clubId = club.clubId,
                name = club.name,
                description = club.description,
                category = categoryInfo,
                organization = club.organization,
                logoImage = club.logoImage,
                meetingPlace = club.meetingPlace,
                meetingTime = club.meetingTime,
                fee = club.fee,
                joinMethod = club.joinMethod,
                visibility = club.visibility,
                createdBy = club.createdBy,
                createdAt = club.createdAt,
                updatedAt = club.updatedAt,
                memberCount = memberCount,
                myRole = membership?.role,
                isJoined = membership != null
            )
        }

        return ClubListResponse(
            success = true,
            message = "공개 동아리 목록 조회 성공",
            clubs = clubInfos,
            totalCount = clubInfos.size.toLong()
        )
    }

    // 내가 속한 동아리 목록 조회
    @Transactional(readOnly = true)
    fun getMyClubs(userId: Long): List<ClubInfo> {
        val clubs = clubRepository.findMyClubs(userId)

        return clubs.map { club ->
            val memberCount = clubMemberRepository.countByClubIdAndStatus(club.clubId, MemberStatus.active)
            val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, club.clubId, MemberStatus.active)
            val categoryInfo = club.category?.let {
                clubCategoryRepository.findByIdOrNull(it)
            }

            ClubInfo(
                clubId = club.clubId,
                name = club.name,
                description = club.description,
                category = categoryInfo,
                organization = club.organization,
                logoImage = club.logoImage,
                meetingPlace = club.meetingPlace,
                meetingTime = club.meetingTime,
                fee = club.fee,
                joinMethod = club.joinMethod,
                visibility = club.visibility,
                createdBy = club.createdBy,
                createdAt = club.createdAt,
                updatedAt = club.updatedAt,
                memberCount = memberCount,
                myRole = membership?.role,
                isJoined = membership != null
            )
        }
    }

    // 동아리 수정
    fun updateClub(clubId: Long, request: UpdateClubRequest, userId: Long): ClubInfo {
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 권한 확인 (소유자 또는 관리자만 수정 가능)
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        if (membership.role != MemberRole.owner && membership.role != MemberRole.staff) {
            throw IllegalArgumentException("동아리를 수정할 권한이 없습니다.")
        }

        // 동아리명 중복 검사 (변경하는 경우)
        if (request.name != null && request.name != club.name) {
            if (clubRepository.existsByName(request.name)) {
                throw IllegalArgumentException("이미 존재하는 동아리명입니다.")
            }
        }

        // 카테고리 확인 (변경하는 경우)
        val categoryId = request.category?.let {
            clubCategoryRepository.findByNameIgnoreCase(it)
                .orElseThrow { IllegalArgumentException("존재하지 않는 카테고리입니다: $it") }
                .categoryId
        } ?: club.category

        // 로고 이미지 처리 (변경하는 경우)
        val logoImagePath = when {
            request.logoImage != null -> saveClubLogo(request.logoImage)
            else -> club.logoImage
        }

        // 동아리 정보 업데이트
        val updatedClub = club.copy(
            name = request.name ?: club.name,
            description = request.description ?: club.description,
            category = categoryId,
            organization = request.organization ?: club.organization,
            logoImage = logoImagePath,
            meetingPlace = request.meetingPlace ?: club.meetingPlace,
            meetingTime = request.meetingTime ?: club.meetingTime,
            fee = request.fee ?: club.fee,
            joinMethod = request.joinMethod ?: club.joinMethod,
            visibility = request.visibility ?: club.visibility,
            updatedAt = LocalDateTime.now(),
            isActive = request.isActive ?: club.isActive
        )

        val savedClub = clubRepository.save(updatedClub)

        logger.info("동아리 수정 완료: clubId=$clubId")

        return getClub(savedClub.clubId, userId)
    }

    // 동아리 비활성화
    fun deactivateClub(clubId: Long, userId: Long) {
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 권한 확인 (소유자만 삭제 가능)
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        if (membership.role != MemberRole.owner) {
            throw IllegalArgumentException("동아리를 삭제할 권한이 없습니다.")
        }

        // 물리적 삭제 대신 비활성화
        val updatedClub = club.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )

        clubRepository.save(updatedClub)

        logger.info("동아리 삭제 완료: clubId=$clubId")
    }

    // 초대 링크 생성
    fun generateInviteLink(clubId: Long, userId: Long): InviteLinkResponse {
        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        // 권한 확인 (멤버만 초대 링크 생성 가능)
        val membership = clubMemberRepository.findByUserIdAndClubIdAndStatus(userId, clubId, MemberStatus.active)
            ?: throw IllegalArgumentException("동아리 멤버가 아닙니다.")

        val inviteCode = inviteCodeGenerator.generateInviteCode(clubId)
        val expiresAt = LocalDateTime.now().plusDays(7)

        // 동적으로 초대 URL 생성
        val inviteUrl = generateInviteUrl(inviteCode)

        logger.info("초대 링크 생성 완료: clubId=$clubId")

        return InviteLinkResponse(
            success = true,
            message = "초대 링크가 생성되었습니다.",
            inviteCode = inviteCode,
            inviteUrl = inviteUrl,
            expiresAt = expiresAt
        )
    }

    // 초대 코드로 동아리 가입
    fun joinClubByInviteCode(request: JoinClubRequest, userId: Long): JoinClubResponse {
        // 초대 코드 유효성 검사 및 동아리 ID 추출
        val clubId = inviteCodeGenerator.decodeInviteCode(request.inviteCode)
            ?: throw IllegalArgumentException("유효하지 않거나 만료된 초대 코드입니다.")

        val club = clubRepository.findByIdOrNull(clubId)
            ?: throw IllegalArgumentException("존재하지 않는 동아리입니다.")

        if (!club.isActive) {
            throw IllegalArgumentException("비활성화된 동아리입니다.")
        }

        // 이미 가입했는지 확인
        val existingMembership = clubMemberRepository.findByUserIdAndClubId(userId, clubId)
        if (existingMembership != null) {
            if (existingMembership.status == MemberStatus.active) {
                throw IllegalArgumentException("이미 가입한 동아리입니다.")
            } else {
                // 비활성 상태에서 재가입
                val reactivatedMember = existingMembership.copy(
                    status = MemberStatus.active,
                    joinedAt = LocalDateTime.now()
                )
                clubMemberRepository.save(reactivatedMember)

                return JoinClubResponse(
                    success = true,
                    message = "동아리에 재가입되었습니다.",
                    clubId = clubId,
                    role = reactivatedMember.role
                )
            }
        }

        // 새 멤버 등록
        val newMember = ClubMember(
            userId = userId,
            clubId = clubId,
            role = MemberRole.member,
            status = MemberStatus.active,
            joinedAt = LocalDateTime.now()
        )

        clubMemberRepository.save(newMember)

        logger.info("동아리 가입 완료: clubId=$clubId, userId=$userId")

        return JoinClubResponse(
            success = true,
            message = "동아리에 가입되었습니다.",
            clubId = clubId,
            role = MemberRole.member
        )
    }

    // 동아리 로고 이미지 저장
    private fun saveClubLogo(base64Image: String): String {
        return try {
            val imageBytes = Base64.getDecoder().decode(base64Image)
            val fileName = "${UUID.randomUUID()}.jpg"

            val uploadDir = Paths.get(uploadPath)
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            val filePath = uploadDir.resolve(fileName)
            Files.write(filePath, imageBytes)

            fileName

        } catch (e: IOException) {
            logger.error("동아리 로고 이미지 저장 실패", e)
            throw RuntimeException("동아리 로고 이미지 저장에 실패했습니다.")
        }
    }

    // 초대 URL 생성 (환경에 따라 다른 방식 사용)
    private fun generateInviteUrl(inviteCode: String): String {
        return when {
            // 안드로이드 앱용 딥링크
            deepLinkScheme.isNotEmpty() -> "$deepLinkScheme://join?code=$inviteCode"
            // 웹용 또는 Universal Link
            else -> "$baseUrl/api/club/join?code=$inviteCode"
        }
    }
}