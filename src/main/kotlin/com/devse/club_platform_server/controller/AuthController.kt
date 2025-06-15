package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.*
import com.devse.club_platform_server.service.AuthService
import com.devse.club_platform_server.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    // 기존 JSON 방식 회원가입 (하위 호환성 유지)
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

    // 새로운 Multipart 방식 회원가입 (프론트엔드 호환)
    @PostMapping("/register-with-image", consumes = ["multipart/form-data"])
    fun registerWithImage(
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        @RequestParam("name") name: String,
        @RequestParam("university") university: String,
        @RequestParam("department") department: String,
        @RequestParam("major") major: String,
        @RequestParam("studentId") studentId: String,
        @RequestParam("profileImage", required = false) profileImage: MultipartFile?
    ): ResponseEntity<RegisterResponse> {
        logger.info("Multipart 회원가입 요청: $email")

        return try {
            // 입력값 유효성 검사
            validateRegistrationInput(email, password, name, university, department, major, studentId)

            val response = userService.registerUserWithImage(
                email = email,
                password = password,
                name = name,
                university = university,
                department = department,
                major = major,
                studentId = studentId,
                profileImage = profileImage
            )

            logger.info("Multipart 회원가입 성공: $email")
            ResponseEntity.ok(response)

        } catch (e: IllegalArgumentException) {
            logger.warn("회원가입 실패 - 잘못된 입력: ${e.message}")
            ResponseEntity.badRequest().body(
                RegisterResponse(
                    success = false,
                    message = e.message ?: "입력값이 올바르지 않습니다.",
                    userId = null
                )
            )
        } catch (e: Exception) {
            logger.error("Multipart 회원가입 실패", e)
            ResponseEntity.internalServerError().body(
                RegisterResponse(
                    success = false,
                    message = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    userId = null
                )
            )
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        logger.info("로그인 요청: ${request.email}")

        return try {
            val response = authService.login(request)
            // 로그인 성공 시 UserService의 updateLastLoginAtOnly 호출
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

    @GetMapping("/profile-image/{userId}")
    fun getProfileImage(@PathVariable userId: Long): ResponseEntity<ByteArray> {
        return try {
            val imagePath = userService.getProfileImagePath(userId)
            val path = Paths.get(imagePath)
            val imageBytes = Files.readAllBytes(path)
            val contentType = Files.probeContentType(path) ?: MediaType.APPLICATION_OCTET_STREAM_VALUE

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageBytes)
        } catch (e: IllegalArgumentException) {
            logger.warn("프로필 이미지 조회 실패 (클라이언트 오류): ${e.message}")
            // 한글 메시지는 헤더에 넣지 않고, 바디에 JSON으로 반환
            val errorJson = """{"success":false,"message":"${e.message ?: "Not found"}"}"""
            ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorJson.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            logger.error("프로필 이미지 조회 실패 (서버 오류): ", e)
            ResponseEntity.internalServerError().build()
        }
    }

    // 프로필 사진 변경 (Base64 방식)
    @PostMapping("/profile-image/update")
    fun updateProfileImageBase64(
        @RequestBody request: UpdateProfileImageBase64Request
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val fileName = userService.updateProfileImageBase64(request.userId, request.base64Image)
            ResponseEntity.ok(mapOf("success" to true, "fileName" to fileName))
        } catch (e: Exception) {
            logger.error("프로필 이미지 변경 실패: ", e)
            ResponseEntity.status(400).body(mapOf("success" to false, "message" to (e.message ?: "프로필 이미지 변경 실패")))
        }
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody request: ChangePasswordRequest): ResponseEntity<Map<String, Any>> {
        return try {
            userService.changePasswordWithCheck(
                request.userId,
                request.currentPassword,
                request.newPassword,
                request.confirmPassword
            )
            ResponseEntity.ok(mapOf("success" to true))
        } catch (e: Exception) {
            logger.error("비밀번호 변경 실패: ", e)
            ResponseEntity.status(400).body(mapOf("success" to false, "message" to (e.message ?: "비밀번호 변경 실패")))
        }
    }

    // 회원탈퇴: 비밀번호 검증 후 탈퇴
    @PostMapping("/withdraw")
    fun withdraw(@RequestBody request: WithdrawRequest): ResponseEntity<Map<String, Any>> {
        return try {
            userService.deleteUserWithPasswordCheck(request.userId, request.password)
            ResponseEntity.ok(mapOf("success" to true, "message" to "회원탈퇴가 완료되었습니다."))
        } catch (e: Exception) {
            logger.error("회원탈퇴 실패: ", e)
            ResponseEntity.status(400).body(mapOf("success" to false, "message" to (e.message ?: "회원탈퇴 실패")))
        }
    }

    // 학과정보 변경
    @PostMapping("/update-department")
    fun updateDepartment(@RequestBody request: UpdateDepartmentRequest): ResponseEntity<Map<String, Any>> {
        return try {
            userService.updateDepartmentAndMajor(request.userId, request.department, request.major)
            ResponseEntity.ok(mapOf("success" to true, "message" to "학과정보가 변경되었습니다."))
        } catch (e: Exception) {
            logger.error("학과정보 변경 실패: ", e)
            ResponseEntity.status(400).body(mapOf("success" to false, "message" to (e.message ?: "학과정보 변경 실패")))
        }
    }

    // 입력값 유효성 검사 헬퍼 메서드
    private fun validateRegistrationInput(
        email: String,
        password: String,
        name: String,
        university: String,
        department: String,
        major: String,
        studentId: String
    ) {
        when {
            email.isBlank() -> throw IllegalArgumentException("이메일을 입력해주세요.")
            !email.contains("@") -> throw IllegalArgumentException("올바른 이메일 형식이 아닙니다.")
            password.isBlank() -> throw IllegalArgumentException("비밀번호를 입력해주세요.")
            password.length < 6 -> throw IllegalArgumentException("비밀번호는 6자 이상이어야 합니다.")
            name.isBlank() -> throw IllegalArgumentException("이름을 입력해주세요.")
            university.isBlank() -> throw IllegalArgumentException("대학교를 입력해주세요.")
            department.isBlank() -> throw IllegalArgumentException("학과를 입력해주세요.")
            major.isBlank() -> throw IllegalArgumentException("전공을 입력해주세요.")
            studentId.isBlank() -> throw IllegalArgumentException("학번을 입력해주세요.")
        }
    }
}

data class ChangePasswordRequest(
    val userId: Long,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

// 회원탈퇴 요청 DTO
data class WithdrawRequest(
    val userId: Long,
    val password: String
)

data class UpdateProfileImageBase64Request(
    val userId: Long,
    val base64Image: String
)

data class UpdateDepartmentRequest(
    val userId: Long,
    val department: String,
    val major: String
)