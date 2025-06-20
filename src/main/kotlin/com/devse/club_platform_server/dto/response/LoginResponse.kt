package com.devse.club_platform_server.dto.response

/*
로그인 처리 결과 응답 DTO
- 이메일/비밀번호 인증 완료 후 결과를 반환할 때 사용
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserInfo? = null
)

/*
사용자 기본 정보 응답 DTO
- 로그인 응답이나 프로필 조회 시 사용자 정보를 담는 데이터 클래스
 */
data class UserInfo(
    val userId: Long,
    val email: String,
    val name: String,
    val university: String,
    val department: String,
    val major: String,
    val studentId: String,
    val profileImage: String?
)

/*
토큰 갱신 처리 결과 응답 DTO
- 리프레시 토큰으로 새 액세스 토큰 발급 후 결과를 반환할 때 사용
 */
data class TokenResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val refreshToken: String? = null
)