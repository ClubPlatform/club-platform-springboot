package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.CreateChatRoomRequest
import com.devse.club_platform_server.dto.request.SendMessageRequest
import com.devse.club_platform_server.dto.response.WebSocketMessage
import com.devse.club_platform_server.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ChatServiceTest {

    @Mock
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Mock
    private lateinit var chatRoomMemberRepository: ChatRoomMemberRepository

    @Mock
    private lateinit var messageRepository: MessageRepository

    @Mock
    private lateinit var messageReadStatusRepository: MessageReadStatusRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var messagingTemplate: SimpMessagingTemplate

    @InjectMocks
    private lateinit var chatService: ChatService

    private lateinit var testUser1: User
    private lateinit var testUser2: User
    private lateinit var testChatRoom: ChatRoom

    @BeforeEach
    fun setUp() {
        testUser1 = User(
            userId = 1L,
            email = "user1@test.com",
            password = "password",
            name = "User 1",
            university = "Test University",
            department = "Computer Science",
            major = "Software Engineering",
            studentId = "2021001"
        )

        testUser2 = User(
            userId = 2L,
            email = "user2@test.com",
            password = "password",
            name = "User 2",
            university = "Test University",
            department = "Computer Science",
            major = "Software Engineering",
            studentId = "2021002"
        )

        testChatRoom = ChatRoom(
            chatRoomId = 1L,
            name = "Test Chat Room",
            type = ChatRoomType.personal
        )

        // Reset mocks
        reset(chatRoomRepository, chatRoomMemberRepository, messageRepository,
            messageReadStatusRepository, userRepository, messagingTemplate)
    }

    @Test
    fun `개인 채팅방 생성 테스트`() {
        // Given
        val request = CreateChatRoomRequest(
            name = null,
            type = "personal",
            memberIds = listOf(2L)
        )

        whenever(chatRoomRepository.findPersonalChatRoom(1L, 2L)).thenReturn(null)
        whenever(chatRoomRepository.save(any<ChatRoom>())).thenReturn(testChatRoom)
        whenever(chatRoomMemberRepository.save(any<ChatRoomMember>())).thenAnswer { invocation ->
            invocation.arguments[0] as ChatRoomMember
        }

        // When
        val response = chatService.createChatRoom(request, 1L)

        // Then
        assertTrue(response.success)
        assertEquals(testChatRoom.chatRoomId, response.chatRoomId)
        verify(chatRoomRepository).save(any<ChatRoom>())
        verify(chatRoomMemberRepository, times(2)).save(any<ChatRoomMember>()) // 생성자 + 초대된 멤버
    }

    @Test
    fun `기존 개인 채팅방이 있는 경우 재사용`() {
        // Given
        val request = CreateChatRoomRequest(
            name = null,
            type = "personal",
            memberIds = listOf(2L)
        )

        whenever(chatRoomRepository.findPersonalChatRoom(1L, 2L)).thenReturn(testChatRoom)

        // When
        val response = chatService.createChatRoom(request, 1L)

        // Then
        assertTrue(response.success)
        assertEquals("기존 채팅방을 사용합니다.", response.message)
        assertEquals(testChatRoom.chatRoomId, response.chatRoomId)
        verify(chatRoomRepository, never()).save(any<ChatRoom>())
    }

    @Test
    fun `메시지 전송 테스트`() {
        // Given
        val request = SendMessageRequest(
            chatRoomId = 1L,
            content = "Hello, World!",
            messageType = MessageType.text
        )

        val savedMessage = Message(
            messageId = 1L,
            chatRoomId = 1L,
            senderId = 1L,
            content = "Hello, World!",
            messageType = MessageType.text
        )

        whenever(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testChatRoom))
        whenever(chatRoomMemberRepository.existsByChatRoomIdAndUserId(1L, 1L)).thenReturn(true)
        whenever(messageRepository.save(any<Message>())).thenReturn(savedMessage)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser1))
        whenever(chatRoomRepository.save(any<ChatRoom>())).thenReturn(testChatRoom)

        // When
        val response = chatService.sendMessage(request, 1L)

        // Then
        assertTrue(response.success)
        assertEquals("메시지가 전송되었습니다.", response.message)
        assertEquals(savedMessage.messageId, response.messageId)
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any<WebSocketMessage>())
    }

    @Test
    fun `채팅방 멤버가 아닌 경우 메시지 전송 실패`() {
        // Given
        val request = SendMessageRequest(
            chatRoomId = 1L,
            content = "Hello, World!"
        )

        whenever(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testChatRoom))
        whenever(chatRoomMemberRepository.existsByChatRoomIdAndUserId(1L, 1L)).thenReturn(false)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            chatService.sendMessage(request, 1L)
        }
    }

    @Test
    fun `메시지 읽음 처리 테스트`() {
        // Given
        val member = ChatRoomMember(
            memberId = 1L,
            chatRoomId = 1L,
            userId = 1L,
            lastReadAt = null
        )

        whenever(chatRoomMemberRepository.findByChatRoomIdAndUserId(1L, 1L)).thenReturn(member)
        whenever(chatRoomMemberRepository.save(any<ChatRoomMember>())).thenAnswer { invocation ->
            invocation.arguments[0] as ChatRoomMember
        }

        // When
        chatService.markMessagesAsRead(1L, 1L)

        // Then
        verify(chatRoomMemberRepository).save(any<ChatRoomMember>())
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/1"), any<WebSocketMessage>())

        // lastReadAt이 설정되었는지 확인
        assertNotNull(member.lastReadAt)
    }

    @Test
    fun `채팅방 나가기 테스트`() {
        // Given
        val member = ChatRoomMember(
            memberId = 1L,
            chatRoomId = 1L,
            userId = 1L
        )

        whenever(chatRoomMemberRepository.findByChatRoomIdAndUserId(1L, 1L)).thenReturn(member)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser1))
        whenever(messageRepository.save(any<Message>())).thenAnswer { invocation ->
            invocation.arguments[0] as Message
        }

        // When
        chatService.leaveChatRoom(1L, 1L)

        // Then
        verify(chatRoomMemberRepository).delete(member)
        verify(messageRepository).save(argThat { message ->
            message.messageType == MessageType.system &&
                    message.content.contains("채팅방을 나갔습니다")
        })
    }
}