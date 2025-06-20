package com.devse.club_platform_server.util

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class InviteCodeGenerator {

    private val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val codeLength = 8

    /*
     동아리별 고유한 8자리 가입코드 생성
     - 영문 대문자와 숫자 조합
     - 중복 방지를 위해 호출하는 쪽에서 유니크 체크 필요
     */
    fun generateUniqueInviteCode(): String {
        return (1..codeLength)
            .map { characters[Random.nextInt(characters.length)] }
            .joinToString("")
    }

    /*
     가입코드 유효성 검사
     - 8자리 영문 대문자 + 숫자 조합인지 확인
     */
    fun isValidInviteCode(code: String): Boolean {
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.length != codeLength) return false
        return trimmedCode.all { it.isLetterOrDigit() && (it.isUpperCase() || it.isDigit()) }
    }
}