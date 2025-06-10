package com.devse.club_platform_server.dto

import jakarta.validation.constraints.*

data class RegisterRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    @field:Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
    val name: String,

    @field:NotBlank(message = "대학교는 필수입니다")
    @field:Size(max = 100, message = "대학교명은 100자를 초과할 수 없습니다")
    val university: String,

    @field:NotBlank(message = "학과는 필수입니다")
    @field:Size(max = 100, message = "학과명은 100자를 초과할 수 없습니다")
    val department: String,

    @field:NotBlank(message = "전공은 필수입니다")
    @field:Size(max = 100, message = "전공명은 100자를 초과할 수 없습니다")
    val major: String,

    @field:NotBlank(message = "학번은 필수입니다")
    @field:Size(max = 20, message = "학번은 20자를 초과할 수 없습니다")
    val studentId: String,

    val profileImage: String? = null // Base64 인코딩된 이미지
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Long? = null
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            message = "성공",
            data = data
        )

        fun <T> success(message: String, data: T): ApiResponse<T> = ApiResponse(
            success = true,
            message = message,
            data = data
        )

        fun <T> error(message: String): ApiResponse<T> = ApiResponse(
            success = false,
            message = message,
            data = null
        )
    }
}