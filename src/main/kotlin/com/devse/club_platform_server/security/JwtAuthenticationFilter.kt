package com.devse.club_platform_server.security

import com.devse.club_platform_server.util.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/*
JWT 토큰 기반 인증 필터
- HTTP 요청 헤더에서 Bearer 토큰 추출 및 검증
- 유효한 JWT 토큰을 통한 사용자 인증 정보 설정
- Spring Security Context에 인증 객체 등록
- 모든 보호된 엔드포인트에 대한 토큰 기반 접근 제어
- 인증 실패 시 예외 처리 및 로깅
 */

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            if (jwt != null) {
                logger.info("JWT 토큰 발견: ${jwt.take(20)}...")

                if (jwtTokenProvider.validateToken(jwt)) {
                    val userId = jwtTokenProvider.getUserIdFromToken(jwt)
                    val email = jwtTokenProvider.getEmailFromToken(jwt)

                    logger.info("JWT 토큰 검증 성공: userId=$userId, email=$email")

                    // 간단한 인증 객체 생성
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        emptyList()
                    )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    SecurityContextHolder.getContext().authentication = authentication

                    logger.info("SecurityContext에 인증 정보 설정 완료: principal=$userId")
                } else {
                    logger.warn("JWT 토큰 검증 실패")
                }
            } else {
                logger.debug("Authorization 헤더에 JWT 토큰이 없음")
            }
        } catch (ex: Exception) {
            logger.error("사용자 인증을 설정할 수 없습니다", ex)
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        logger.debug("Authorization 헤더: $bearerToken")

        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}