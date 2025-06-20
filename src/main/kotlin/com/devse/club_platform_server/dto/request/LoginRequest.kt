package com.devse.club_platform_server.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/*
로그인 요청 DTO
- 사용자 인증을 위한 이메일/비밀번호 로그인 시 사용
- 이메일 형식 검증 및 필수 값 검증 포함
 */
data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

/*
토큰 갱신 요청 DTO
- Access Token 만료 시 Refresh Token으로 새 토큰 발급받을 때 사용
- JWT 기반 인증 시스템의 토큰 갱신 처리
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "리프레시 토큰은 필수입니다")
    val refreshToken: String
)