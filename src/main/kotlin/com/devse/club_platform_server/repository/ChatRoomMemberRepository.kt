// ChatRoomMemberRepository.kt
package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.ChatRoomMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomMemberRepository : JpaRepository<ChatRoomMember, Long> {

    // 채팅방의 멤버 목록 조회
    fun findByChatRoomId(chatRoomId: Long): List<ChatRoomMember>

    // 특정 사용자의 특정 채팅방 멤버십 조회
    fun findByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): ChatRoomMember?

    // 사용자가 특정 채팅방의 멤버인지 확인
    fun existsByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): Boolean

    // 채팅방의 멤버 수 조회
    fun countByChatRoomId(chatRoomId: Long): Long

    // 사용자가 속한 채팅방 ID 목록 조회
    @Query("SELECT crm.chatRoomId FROM ChatRoomMember crm WHERE crm.userId = :userId")
    fun findChatRoomIdsByUserId(@Param("userId") userId: Long): List<Long>
}
