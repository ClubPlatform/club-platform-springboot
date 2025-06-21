package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.UserKeyword
import com.devse.club_platform_server.dto.request.AddKeywordRequest
import com.devse.club_platform_server.dto.response.ApiResponse
import com.devse.club_platform_server.dto.response.KeywordListResponse
import com.devse.club_platform_server.repository.UserKeywordRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/*
사용자 키워드 관리 서비스
- 사용자별 알림 키워드 등록/삭제
- 키워드 기반 알림 대상 사용자 조회
 */

@Service
@Transactional
class UserKeywordService(
    private val userKeywordRepository: UserKeywordRepository
) {

    private val logger = LoggerFactory.getLogger(UserKeywordService::class.java)

    // 사용자 키워드 목록 조회
    @Transactional(readOnly = true)
    fun getUserKeywords(userId: Long): KeywordListResponse {
        val keywords = userKeywordRepository.findByUserId(userId)
            .map { it.keyword }

        return KeywordListResponse(
            success = true,
            message = "키워드 목록 조회 성공",
            keywords = keywords
        )
    }

    // 키워드 추가
    fun addKeyword(userId: Long, request: AddKeywordRequest): ApiResponse<String> {
        val keyword = request.keyword.trim()

        // 키워드 유효성 검사
        if (keyword.isBlank()) {
            throw IllegalArgumentException("키워드는 공백일 수 없습니다.")
        }

        if (keyword.length > 50) {
            throw IllegalArgumentException("키워드는 50자를 초과할 수 없습니다.")
        }

        // 중복 키워드 확인
        if (userKeywordRepository.existsByUserIdAndKeyword(userId, keyword)) {
            throw IllegalArgumentException("이미 등록된 키워드입니다.")
        }

        // 사용자당 키워드 개수 제한 (예: 최대 10개)
        val currentKeywords = userKeywordRepository.findByUserId(userId)
        if (currentKeywords.size >= 10) {
            throw IllegalArgumentException("키워드는 최대 10개까지 등록할 수 있습니다.")
        }

        // 키워드 저장
        val userKeyword = UserKeyword(
            userId = userId,
            keyword = keyword,
            createdAt = LocalDateTime.now()
        )

        userKeywordRepository.save(userKeyword)

        logger.info("키워드 추가 완료: userId=$userId, keyword=$keyword")

        return ApiResponse.success("키워드가 추가되었습니다.")
    }

    // 키워드 삭제
    fun deleteKeyword(userId: Long, keyword: String): ApiResponse<String> {
        val deletedCount = userKeywordRepository.deleteByUserIdAndKeyword(userId, keyword.trim())

        if (deletedCount == 0) {
            throw IllegalArgumentException("삭제할 키워드가 없습니다.")
        }

        logger.info("키워드 삭제 완료: userId=$userId, keyword=$keyword")

        return ApiResponse.success("키워드가 삭제되었습니다.")
    }
}