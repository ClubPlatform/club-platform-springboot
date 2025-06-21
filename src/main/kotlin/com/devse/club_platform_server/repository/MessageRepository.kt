// MessageRepository.kt
package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MessageRepository : JpaRepository<Message, Long> {

    // 채팅방의 메시지 목록 조회 (페이징)
    fun findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(
        chatRoomId: Long,
        pageable: Pageable
    ): Page<Message>

    // 특정 시간 이후의 메시지 조회
    fun findByChatRoomIdAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtAsc(
        chatRoomId: Long,
        after: LocalDateTime
    ): List<Message>

    // 채팅방의 마지막 메시지 조회
    fun findTopByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(chatRoomId: Long): Message?

    // 읽지 않은 메시지 수 조회 - 가입 시점 이후의 메시지만 카운트
    @Query("""
        SELECT COUNT(m) FROM Message m
        JOIN ChatRoomMember crm ON crm.chatRoomId = m.chatRoomId AND crm.userId = :userId
        WHERE m.chatRoomId = :chatRoomId
        AND m.isDeleted = false
        AND m.senderId != :userId
        AND m.createdAt > COALESCE(crm.lastReadAt, crm.joinedAt)
    """)
    fun countUnreadMessages(
        @Param("chatRoomId") chatRoomId: Long,
        @Param("userId") userId: Long
    ): Long

    // 특정 시점 이후의 새 메시지 수 조회
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.chatRoomId = :chatRoomId
        AND m.isDeleted = false
        AND m.createdAt > :since
        AND m.senderId != :userId
    """)
    fun countNewMessagesSince(
        @Param("chatRoomId") chatRoomId: Long,
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): Long
}