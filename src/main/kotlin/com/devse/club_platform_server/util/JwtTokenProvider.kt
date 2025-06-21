package com.devse.club_platform_server.util

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.access-token-expiration}")
    private var accessTokenExpiration: Long = 7776000000 // 90일 (밀리초)

    @Value("\${jwt.refresh-token-expiration}")
    private var refreshTokenExpiration: Long = 7776000000 // 90일 (밀리초)

    private val key: Key by lazy { Keys.hmacShaKeyFor(jwtSecret.toByteArray()) }

    // Access Token 생성
    fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    // Refresh Token 생성 (DB 저장 없이 긴 만료시간으로 생성)
    fun generateRefreshToken(userId: Long, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    // 토큰에서 사용자 ID 추출
    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject.toLong()
    }

    // 토큰에서 이메일 추출
    fun getEmailFromToken(token: String): String? {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["email"] as? String
    }

    // 토큰 타입 확인
    fun getTokenType(token: String): String? {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims["type"] as? String
    }

    // 토큰 유효성 검증
    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            // 만료 시간 확인
            claims.expiration.after(Date())
        } catch (ex: JwtException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    // 토큰 만료 여부 확인
    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.expiration.before(Date())
        } catch (ex: Exception) {
            true
        }
    }
}