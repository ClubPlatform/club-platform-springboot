package com.devse.club_platform_server.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class InviteCodeGenerator {

    @Value("\${app.invite.secret-key}")
    private lateinit var secretKey: String

    private val algorithm = "AES"

    // 동아리 ID를 암호화하여 초대 코드 생성
    fun generateInviteCode(clubId: Long): String {
        return try {
            val plainText = "${clubId}:${System.currentTimeMillis()}" // ID와 타임스탬프 조합
            val cipher = Cipher.getInstance(algorithm)
            val keySpec = SecretKeySpec(secretKey.toByteArray().take(16).toByteArray(), algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)

            val encrypted = cipher.doFinal(plainText.toByteArray())
            Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted)
        } catch (e: Exception) {
            throw RuntimeException("초대 코드 생성 실패", e)
        }
    }

    // 초대 코드를 복호화하여 동아리 ID 추출
    fun decodeInviteCode(inviteCode: String): Long? {
        return try {
            val cipher = Cipher.getInstance(algorithm)
            val keySpec = SecretKeySpec(secretKey.toByteArray().take(16).toByteArray(), algorithm)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val encrypted = Base64.getUrlDecoder().decode(inviteCode)
            val decrypted = String(cipher.doFinal(encrypted))

            // "clubId:timestamp" 형태에서 clubId만 추출
            val parts = decrypted.split(":")
            if (parts.size >= 2) {
                val clubId = parts[0].toLong()
                val timestamp = parts[1].toLong()

                // 7일 이내 생성된 코드만 유효
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                if (timestamp > sevenDaysAgo) {
                    clubId
                } else {
                    null // 만료된 코드
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null // 잘못된 코드
        }
    }
}