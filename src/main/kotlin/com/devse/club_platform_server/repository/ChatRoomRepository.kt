// ChatRoomRepository.kt
package com.devse.club_platform_server.repository

import com.devse.club_platform_server.domain.ChatRoom
import com.devse.club_platform_server.domain.ChatRoomType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {

    // 사용자가 속한 채팅방 목록 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        JOIN ChatRoomMember crm ON cr.chatRoomId = crm.chatRoomId
        WHERE crm.userId = :userId
        ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC
    """)
    fun findByUserId(@Param("userId") userId: Long): List<ChatRoom>

    // 두 사용자 간의 개인 채팅방 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.type = 'personal'
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm1
            WHERE crm1.chatRoomId = cr.chatRoomId AND crm1.userId = :userId1
        )
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm2
            WHERE crm2.chatRoomId = cr.chatRoomId AND crm2.userId = :userId2
        )
    """)
    fun findPersonalChatRoom(
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long
    ): ChatRoom?
}
