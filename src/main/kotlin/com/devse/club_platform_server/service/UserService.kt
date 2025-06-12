package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.User
import com.devse.club_platform_server.dto.RegisterRequest
import com.devse.club_platform_server.dto.RegisterResponse
import com.devse.club_platform_server.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // 프로필 이미지 저장 경로 (application.properties에서 설정 가능)
    @Value("\${app.upload.path:uploads/profiles/}")
    private lateinit var uploadPath: String

    // Multipart 파일을 받는 회원가입 메서드
    fun registerUserWithImage(
        email: String,
        password: String,
        name: String,
        university: String,
        department: String,
        major: String,
        studentId: String,
        profileImage: MultipartFile?
    ): RegisterResponse {
        logger.info("회원가입 시도: $email")

        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 사용중인 이메일입니다.")
        }

        // 학번 중복 검사
        if (userRepository.existsByStudentId(studentId)) {
            throw IllegalArgumentException("이미 사용중인 학번입니다.")
        }

        // 프로필 이미지 처리
        val profileImagePath = profileImage?.let {
            saveProfileImage(it)
        }

        // 사용자 생성
        val user = User(
            email = email,
            password = passwordEncoder.encode(password),
            name = name,
            university = university,
            department = department,
            major = major,
            studentId = studentId,
            profileImage = profileImagePath
        )

        val savedUser = userRepository.save(user)
        logger.info("회원가입 완료: userId=${savedUser.userId}, email=${savedUser.email}")

        return RegisterResponse(
            success = true,
            message = "회원가입이 완료되었습니다.",
            userId = savedUser.userId
        )
    }

    // 기존 Base64 방식도 유지 (하위 호환성)
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

        // 프로필 이미지 처리 (Base64)
        val profileImagePath = request.profileImage?.let {
            saveProfileImageFromBase64(it)
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
            profileImage = profileImagePath
        )

        val savedUser = userRepository.save(user)
        logger.info("회원가입 완료: userId=${savedUser.userId}, email=${savedUser.email}")

        return RegisterResponse(
            success = true,
            message = "회원가입이 완료되었습니다.",
            userId = savedUser.userId
        )
    }

    private fun saveProfileImage(file: MultipartFile): String {
        return try {
            // 파일 유효성 검사
            if (file.isEmpty) {
                throw IllegalArgumentException("파일이 비어있습니다.")
            }

            // 파일 크기 제한 (5MB)
            if (file.size > 5 * 1024 * 1024) {
                throw IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.")
            }

            // 파일 타입 검사
            val contentType = file.contentType
            if (contentType == null || !contentType.startsWith("image/")) {
                throw IllegalArgumentException("이미지 파일만 업로드 가능합니다.")
            }

            // 파일 확장자 추출
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast('.', "jpg")

            // 고유한 파일명 생성
            val fileName = "${UUID.randomUUID()}.$extension"

            // 업로드 디렉토리 생성
            val uploadDir = Paths.get(uploadPath)
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            // 파일 저장
            val filePath = uploadDir.resolve(fileName)
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            logger.info("프로필 이미지 저장 완료: $fileName")
            fileName // 파일명만 반환

        } catch (e: IOException) {
            logger.error("프로필 이미지 저장 실패", e)
            throw RuntimeException("프로필 이미지 저장에 실패했습니다.")
        }
    }

    private fun saveProfileImageFromBase64(base64Image: String): String {
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

            fileName // 파일명만 반환
        } catch (e: IOException) {
            logger.error("프로필 이미지 저장 실패", e)
            throw RuntimeException("프로필 이미지 저장에 실패했습니다.")
        }
    }

    // userId로 프로필 이미지 전체 경로 반환
    fun getProfileImagePath(userId: Long): String {
        val user = userRepository.findById(userId).orElseThrow {
            logger.warn("userId=$userId 사용자를 찾을 수 없습니다.")
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        val fileName = user.profileImage
        if (fileName.isNullOrBlank()) {
            logger.warn("userId=$userId 의 profileImage 필드가 비어있음")
            throw IllegalArgumentException("프로필 이미지가 없습니다.")
        }
        val filePath = Paths.get(uploadPath, fileName).toAbsolutePath().normalize()
        if (!Files.exists(filePath)) {
            logger.warn("userId=$userId 의 프로필 이미지 파일이 실제로 존재하지 않음: $filePath")
            throw IllegalArgumentException("프로필 이미지 파일이 존재하지 않습니다.")
        }
        return filePath.toString()
    }

    // 기존 비밀번호 검증 후 새 비밀번호로 변경
    fun changePasswordWithCheck(userId: Long, currentPassword: String, newPassword: String, confirmPassword: String) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.")
        }
        if (newPassword != confirmPassword) {
            throw IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        }
        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)
    }

    // 비밀번호 검증 후 회원탈퇴
    fun deleteUserWithPasswordCheck(userId: Long, inputPassword: String) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        if (!passwordEncoder.matches(inputPassword, user.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }
        userRepository.delete(user)
    }

    // 프로필 사진 변경 (Base64 방식)
    fun updateProfileImageBase64(userId: Long, base64Image: String): String {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }

        // 기존 이미지 삭제 (있을 경우)
        user.profileImage?.let { oldFileName ->
            val oldFilePath = Paths.get(uploadPath, oldFileName)
            try {
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath)
                    logger.info("기존 프로필 이미지 삭제: $oldFilePath")
                }
            } catch (e: Exception) {
                logger.warn("기존 프로필 이미지 삭제 실패: $oldFilePath", e)
            }
        }

        // 새 이미지 저장 (Base64)
        val newFileName = saveProfileImageFromBase64(base64Image)
        user.profileImage = newFileName
        userRepository.save(user)
        return newFileName
    }

    // 학과정보 변경
    fun updateDepartmentAndMajor(userId: Long, department: String, major: String) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다.")
        }
        user.department = department
        user.major = major
        userRepository.save(user)
    }
}
