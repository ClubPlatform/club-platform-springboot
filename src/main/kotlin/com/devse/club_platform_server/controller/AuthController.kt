package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.*
import com.devse.club_platform_server.service.AuthService
import com.devse.club_platform_server.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        logger.info("회원가입 요청: ${request.email}")

        return try {
            val response = userService.registerUser(request)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("회원가입 실패", e)

            val errorResponse = RegisterResponse(
                success = false,
                message = e.message ?: "회원가입에 실패했습니다.",
                userId = null
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info("로그인 요청: ${request.email}")

        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("로그인 실패: ${e.message}")

            val errorResponse = LoginResponse(
                success = false,
                message = e.message ?: "로그인에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.info("토큰 갱신 요청")

        return try {
            val response = authService.refreshToken(request)
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("토큰 갱신 실패: ${e.message}")

            val errorResponse = TokenResponse(
                success = false,
                message = e.message ?: "토큰 갱신에 실패했습니다."
            )

            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<String>> {
        logger.info("로그아웃 요청")

        authService.logout()

        return ResponseEntity.ok(
            ApiResponse.success("로그아웃 되었습니다. 클라이언트에서 토큰을 삭제해주세요.", "")
        )
    }

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<ApiResponse<UserInfo>> {
        val userId = authentication.principal as Long

        return try {
            val userInfo = authService.getUserInfo(userId)
            ResponseEntity.ok(ApiResponse.success(userInfo))

        } catch (e: Exception) {
            logger.error("사용자 정보 조회 실패: ${e.message}")

            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "사용자 정보를 조회할 수 없습니다."))
        }
    }

}