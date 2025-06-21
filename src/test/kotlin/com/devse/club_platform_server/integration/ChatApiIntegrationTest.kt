package com.devse.club_platform_server.integration

import com.devse.club_platform_server.domain.User
import com.devse.club_platform_server.dto.LoginRequest
import com.devse.club_platform_server.dto.request.CreateChatRoomRequest
import com.devse.club_platform_server.dto.request.SendMessageRequest
import com.devse.club_platform_server.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatApiIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var accessToken: String? = null
    private var userId1: Long? = null
    private var userId2: Long? = null

    @BeforeEach
    fun setUp() {
        // 데이터베이스 초기화
        userRepository.deleteAll()

        // 테스트 사용자 생성
        val user1 = userRepository.save(User(
            email = "chatuser1@test.com",
            password = passwordEncoder.encode("password123"),
            name = "Chat User 1",
            university = "Test University",
            department = "Computer Science",
            major = "Software Engineering",
            studentId = "2024001"
        ))
        userId1 = user1.userId

        val user2 = userRepository.save(User(
            email = "chatuser2@test.com",
            password = passwordEncoder.encode("password123"),
            name = "Chat User 2",
            university = "Test University",
            department = "Computer Science",
            major = "Software Engineering",
            studentId = "2024002"
        ))
        userId2 = user2.userId

        // 로그인하여 토큰 획득
        login()
    }

    private fun login() {
        val loginRequest = LoginRequest("chatuser1@test.com", "password123")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body["success"] as Boolean)
        accessToken = body["accessToken"] as String
    }

    @Test
    fun `채팅방 생성 API 테스트`() {
        // Given
        val request = CreateChatRoomRequest(
            name = null,
            type = "personal",
            memberIds = listOf(userId2!!)
        )

        val headers = createAuthHeaders()
        val entity = HttpEntity(request, headers)

        // When
        val response = restTemplate.postForEntity(
            "/api/chats/rooms",
            entity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body["success"] as Boolean)
        assertNotNull(body["chatRoomId"])
    }

    @Test
    fun `메시지 전송 API 테스트`() {
        // Given - 먼저 채팅방 생성
        val createRoomRequest = CreateChatRoomRequest(
            name = null,
            type = "personal",
            memberIds = listOf(userId2!!)
        )

        val headers = createAuthHeaders()
        val roomEntity = HttpEntity(createRoomRequest, headers)

        val roomResponse = restTemplate.postForEntity(
            "/api/chats/rooms",
            roomEntity,
            Map::class.java
        )

        val chatRoomId = (roomResponse.body!!["chatRoomId"] as Number).toLong()

        // When - 메시지 전송
        val sendMessageRequest = SendMessageRequest(
            chatRoomId = chatRoomId,
            content = "Hello from integration test!"
        )

        val messageEntity = HttpEntity(sendMessageRequest, headers)

        val messageResponse = restTemplate.postForEntity(
            "/api/chats/messages",
            messageEntity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, messageResponse.statusCode)
        val body = messageResponse.body!!
        assertTrue(body["success"] as Boolean)
        assertNotNull(body["messageId"])
    }

    @Test
    fun `채팅방 목록 조회 API 테스트`() {
        // Given - 채팅방 생성
        createTestChatRoom()

        // When
        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(
            "/api/chats/rooms",
            org.springframework.http.HttpMethod.GET,
            entity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body["success"] as Boolean)
        assertNotNull(body["chatRooms"])

        @Suppress("UNCHECKED_CAST")
        val chatRooms = body["chatRooms"] as List<Map<String, Any>>
        assertTrue(chatRooms.isNotEmpty())
    }

    @Test
    fun `메시지 목록 조회 API 테스트`() {
        // Given - 채팅방 생성 및 메시지 전송
        val chatRoomId = createTestChatRoom()
        sendTestMessage(chatRoomId, "Test message 1")
        sendTestMessage(chatRoomId, "Test message 2")

        // When
        val headers = createAuthHeaders()
        val entity = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(
            "/api/chats/rooms/$chatRoomId/messages?page=0&size=50",
            org.springframework.http.HttpMethod.GET,
            entity,
            Map::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body!!
        assertTrue(body["success"] as Boolean)
        assertNotNull(body["messages"])

        @Suppress("UNCHECKED_CAST")
        val messages = body["messages"] as List<Map<String, Any>>
        assertEquals(2, messages.size)
    }

    private fun createAuthHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(accessToken!!)
        return headers
    }

    private fun createTestChatRoom(): Long {
        val request = CreateChatRoomRequest(
            name = "Test Chat Room",
            type = "group",
            memberIds = listOf(userId2!!)
        )

        val headers = createAuthHeaders()
        val entity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(
            "/api/chats/rooms",
            entity,
            Map::class.java
        )

        return (response.body!!["chatRoomId"] as Number).toLong()
    }

    private fun sendTestMessage(chatRoomId: Long, content: String) {
        val request = SendMessageRequest(
            chatRoomId = chatRoomId,
            content = content
        )

        val headers = createAuthHeaders()
        val entity = HttpEntity(request, headers)

        restTemplate.postForEntity(
            "/api/chats/messages",
            entity,
            Map::class.java
        )
    }
}