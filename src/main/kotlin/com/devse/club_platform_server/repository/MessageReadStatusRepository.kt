// MessageReadStatusRepository.kt
package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.MessageReadStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageReadStatusRepository : JpaRepository<MessageReadStatus, Long> {

    // 메시지를 읽은 사용자 수 조회
    fun countByMessageId(messageId: Long): Long

    // 특정 사용자가 특정 메시지를 읽었는지 확인
    fun existsByMessageIdAndUserId(messageId: Long, userId: Long): Boolean

    // 메시지의 읽음 상태 목록 조회
    fun findByMessageId(messageId: Long): List<MessageReadStatus>
}