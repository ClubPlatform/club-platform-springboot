package com.devse.club_platform_server.dto.response

/*
회원가입 처리 결과 응답 DTO
- 신규 사용자 계정 생성 완료 후 결과를 반환할 때 사용
 */

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: Long? = null
)