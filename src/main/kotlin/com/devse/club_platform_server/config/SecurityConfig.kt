package com.devse.club_platform_server.config

import com.devse.club_platform_server.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.http.HttpMethod

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf.disable()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 관련 엔드포인트
                    .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/register-with-image",
                        "/api/auth/login",
                        "/api/auth/refresh"
                    ).permitAll()

                    // 정적 리소스
                    .requestMatchers("/uploads/**").permitAll()

                    // 프로필 이미지 조회는 인증 없이 허용
                    .requestMatchers(HttpMethod.GET, "/api/auth/profile-image/**").permitAll()

                    // WebSocket 엔드포인트
                    .requestMatchers("/ws-chat/**").permitAll()

                    // 채팅 이미지 조회는 인증 없이 허용
                    .requestMatchers(HttpMethod.GET, "/uploads/chats/**").permitAll()

                    // OPTIONS 요청은 모두 허용 (CORS preflight)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Actuator 엔드포인트 (헬스체크용)
                    .requestMatchers("/actuator/health").permitAll()

                    // 나머지는 모두 인증 필요
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            // 예외 처리
            .exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint { request, response, authException ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = 401
                        response.writer.write(
                            """
                            {
                                "success": false,
                                "message": "인증이 필요합니다.",
                                "error": "Unauthorized"
                            }
                        """.trimIndent()
                        )
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = 403
                        response.writer.write(
                            """
                            {
                                "success": false,
                                "message": "접근 권한이 없습니다.",
                                "error": "Access Denied"
                            }
                        """.trimIndent()
                        )
                    }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // 허용할 Origin 패턴
            allowedOriginPatterns = listOf(
                "*" // 개발 중에는 모든 origin 허용
            )

            // 허용할 HTTP 메서드
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

            // 허용할 헤더
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type")

            // 인증 정보 포함 허용
            allowCredentials = true

            // preflight 요청 캐시 시간 (1시간)
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}