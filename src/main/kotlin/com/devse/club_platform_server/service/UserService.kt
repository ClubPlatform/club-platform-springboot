package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.User
import com.devse.club_platform_server.dto.RegisterRequest
import com.devse.club_platform_server.dto.RegisterResponse
import com.devse.club_platform_server.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // 프로필 이미지 저장 경로 (application.properties에서 설정 가능)
    private val uploadPath = "uploads/profiles/"

    fun registerUser(request: RegisterRequest): RegisterResponse {
        logger.info("회원가입 시도: ${request.email}")

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용중인 이메일입니다.")
        }

        // 학번 중복 검사
        if (userRepository.existsByStudentId(request.studentId)) {
            throw IllegalArgumentException("이미 사용중인 학번입니다.")
        }

        // 프로필 이미지 처리
        val profileImagePath = request.profileImage?.let {
            saveProfileImage(it)
        }

        // 사용자 생성
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            university = request.university,
            department = request.department,
            major = request.major,
            studentId = request.studentId,
            profileImage = profileImagePath,
        )

        val savedUser = userRepository.save(user)

        logger.info("회원가입 완료: userId=${savedUser.userId}, email=${savedUser.email}")

        return RegisterResponse(
            success = true,
            message = "회원가입이 완료되었습니다.",
            userId = savedUser.userId
        )
    }

    private fun saveProfileImage(base64Image: String): String {
        return try {
            // Base64 디코딩
            val imageBytes = Base64.getDecoder().decode(base64Image)

            // 파일명 생성 (UUID + 확장자)
            val fileName = "${UUID.randomUUID()}.jpg"

            // 업로드 디렉토리 생성
            val uploadDir = Paths.get(uploadPath)
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            // 파일 저장
            val filePath = uploadDir.resolve(fileName)
            Files.write(filePath, imageBytes)

            fileName // 파일명만 반환 (전체 경로는 서비스에서 조합)

        } catch (e: IOException) {
            logger.error("프로필 이미지 저장 실패", e)
            throw RuntimeException("프로필 이미지 저장에 실패했습니다.")
        }
    }

}
