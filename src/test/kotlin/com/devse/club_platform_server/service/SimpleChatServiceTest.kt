package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.CreateChatRoomRequest
import com.devse.club_platform_server.repository.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SimpleChatServiceTest {

    @Autowired
    private lateinit var chatService: ChatService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    private lateinit var chatRoomMemberRepository: ChatRoomMemberRepository

    @Test
    fun `채팅방 생성 통합 테스트`() {
        // Given - 테스트 사용자 생성
        val user1 = userRepository.save(User(
            email = "test1@test.com",
            password = "password",
            name = "Test User 1",
            university = "Test University",
            department = "CS",
            major = "Software",
            studentId = "2024001"
        ))

        val user2 = userRepository.save(User(
            email = "test2@test.com",
            password = "password",
            name = "Test User 2",
            university = "Test University",
            department = "CS",
            major = "Software",
            studentId = "2024002"
        ))

        val request = CreateChatRoomRequest(
            name = null,
            type = "personal",
            memberIds = listOf(user2.userId)
        )

        // When
        val response = chatService.createChatRoom(request, user1.userId)

        // Then
        assertTrue(response.success)
        assertNotNull(response.chatRoomId)

        // 채팅방이 실제로 생성되었는지 확인
        val chatRoom = chatRoomRepository.findById(response.chatRoomId!!).orElse(null)
        assertNotNull(chatRoom)
        assertEquals(ChatRoomType.personal, chatRoom.type)

        // 멤버가 추가되었는지 확인
        val members = chatRoomMemberRepository.findByChatRoomId(response.chatRoomId!!)
        assertEquals(2, members.size)
    }
}