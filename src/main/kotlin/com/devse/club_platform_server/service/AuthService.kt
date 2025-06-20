package com.devse.club_platform_server.service

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.UserRepository
import com.devse.club_platform_server.util.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/*
사용자 인증 및 토큰 관리 서비스
- 이메일/비밀번호 기반 로그인 처리
- JWT 액세스/리프레시 토큰 생성 및 갱신
- 로그인 상태 관리 및 사용자 정보 조회
- 보안 토큰 검증 및 인증 상태 유지
 */

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): LoginResponse {
        logger.info("로그인 시도: ${request.email}")

        // 사용자 조회
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("비밀번호가 올바르지 않습니다.")
        }

        // 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(user.userId, user.email)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.userId, user.email)

        // 마지막 로그인 시간 업데이트 (updated_at 갱신 방지)
        userService.updateLastLoginAtOnly(user.userId)

        logger.info("로그인 성공: userId=${user.userId}, email=${user.email}")

        return LoginResponse(
            success = true,
            message = "로그인 성공",
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserInfo(
                userId = user.userId,
                email = user.email,
                name = user.name,
                university = user.university,
                department = user.department,
                major = user.major,
                studentId = user.studentId,
                profileImage = user.profileImage
            )
        )
    }

    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        logger.info("토큰 갱신 요청")

        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")
        }

        // 토큰 타입 확인
        val tokenType = jwtTokenProvider.getTokenType(request.refreshToken)
        if (tokenType != "refresh") {
            throw IllegalArgumentException("리프레시 토큰이 아닙니다.")
        }

        // 토큰에서 사용자 정보 추출
        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
        val email = jwtTokenProvider.getEmailFromToken(request.refreshToken)
            ?: throw IllegalArgumentException("토큰에서 이메일 정보를 찾을 수 없습니다.")

        // 사용자 존재 여부 확인
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }

        // 새로운 액세스 토큰 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.userId, user.email)

        logger.info("토큰 갱신 성공: userId=${user.userId}")

        return TokenResponse(
            success = true,
            message = "토큰 갱신 성공",
            accessToken = newAccessToken,
            refreshToken = request.refreshToken // 기존 리프레시 토큰 유지
        )
    }

    // 로그아웃은 클라이언트에서만 처리 (서버에서는 로그만 남김)
    fun logout() {
        logger.info("로그아웃 요청 - 클라이언트에서 토큰 삭제 필요")
        // DB에 저장된 토큰이 없으므로 서버에서 할 일이 없음
    }

    // 사용자 정보 조회 (인증된 사용자용)
    fun getUserInfo(userId: Long): UserInfo {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }

        return UserInfo(
            userId = user.userId,
            email = user.email,
            name = user.name,
            university = user.university,
            department = user.department,
            major = user.major,
            studentId = user.studentId,
            profileImage = user.profileImage
        )
    }
}