package com.devse.club_platform_server.service

import com.devse.club_platform_server.domain.*
import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.repository.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomMemberRepository: ChatRoomMemberRepository,
    private val messageRepository: MessageRepository,
    private val messageReadStatusRepository: MessageReadStatusRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    @Value("\${file.upload-dir:./uploads}")
    private lateinit var uploadDir: String

    private val chatUploadPath = "chats/"

    // 채팅방 생성
    fun createChatRoom(request: CreateChatRoomRequest, creatorId: Long): CreateChatRoomResponse {
        logger.info("채팅방 생성 요청: type=${request.type}, creatorId=$creatorId, memberIds=${request.memberIds}")

        val chatRoomType = try {
            ChatRoomType.valueOf(request.type)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("유효하지 않은 채팅방 타입입니다: ${request.type}")
        }

        // 개인 채팅방인 경우 이미 존재하는지 확인
        if (chatRoomType == ChatRoomType.personal && request.memberIds.size == 1) {
            val existingRoom = chatRoomRepository.findPersonalChatRoom(creatorId, request.memberIds[0])
            if (existingRoom != null) {
                logger.info("기존 개인 채팅방 발견: chatRoomId=${existingRoom.chatRoomId}")

                // 기존 채팅방의 멤버 확인
                val members = chatRoomMemberRepository.findByChatRoomId(existingRoom.chatRoomId)
                logger.info("기존 채팅방 멤버: ${members.map { "userId=${it.userId}" }.joinToString(", ")}")

                return CreateChatRoomResponse(
                    success = true,
                    message = "기존 채팅방을 사용합니다.",
                    chatRoomId = existingRoom.chatRoomId
                )
            }
        }

        // 채팅방 생성
        val chatRoom = ChatRoom(
            name = request.name,
            type = chatRoomType
        )
        val savedChatRoom = chatRoomRepository.save(chatRoom)
        logger.info("채팅방 생성됨: chatRoomId=${savedChatRoom.chatRoomId}")

        // 생성자를 멤버로 추가
        val creatorMember = ChatRoomMember(
            chatRoomId = savedChatRoom.chatRoomId,
            userId = creatorId
        )
        val savedCreatorMember = chatRoomMemberRepository.save(creatorMember)
        logger.info("생성자 멤버 추가됨: memberId=${savedCreatorMember.memberId}, userId=$creatorId")

        // 초대된 멤버들 추가
        request.memberIds.forEach { memberId ->
            if (memberId != creatorId) { // 중복 방지
                val member = ChatRoomMember(
                    chatRoomId = savedChatRoom.chatRoomId,
                    userId = memberId
                )
                val savedMember = chatRoomMemberRepository.save(member)
                logger.info("초대 멤버 추가됨: memberId=${savedMember.memberId}, userId=$memberId")
            }
        }

        // 최종 멤버 확인
        val finalMembers = chatRoomMemberRepository.findByChatRoomId(savedChatRoom.chatRoomId)
        logger.info("최종 채팅방 멤버 수: ${finalMembers.size}, 멤버: ${finalMembers.map { "userId=${it.userId}" }.joinToString(", ")}")

        return CreateChatRoomResponse(
            success = true,
            message = "채팅방이 생성되었습니다.",
            chatRoomId = savedChatRoom.chatRoomId
        )
    }

    // 메시지 목록 조회
    @Transactional(readOnly = true)
    fun getMessageList(chatRoomId: Long, userId: Long, page: Int, size: Int): MessageListResponse {
        logger.info("메시지 목록 조회 시작: chatRoomId=$chatRoomId, userId=$userId, page=$page")

        // 채팅방 존재 확인
        val chatRoomExists = chatRoomRepository.existsById(chatRoomId)
        logger.info("채팅방 존재 여부: chatRoomId=$chatRoomId, exists=$chatRoomExists")

        if (!chatRoomExists) {
            throw IllegalArgumentException("존재하지 않는 채팅방입니다.")
        }

        // 채팅방의 모든 멤버 조회
        val allMembers = chatRoomMemberRepository.findByChatRoomId(chatRoomId)
        logger.info("채팅방 전체 멤버 수: ${allMembers.size}")
        allMembers.forEach { member ->
            logger.info("멤버 정보: memberId=${member.memberId}, userId=${member.userId}, joinedAt=${member.joinedAt}")
        }

        // 현재 사용자의 멤버십 확인
        val userMembership = chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
        if (userMembership != null) {
            logger.info("사용자 멤버십 발견: memberId=${userMembership.memberId}, joinedAt=${userMembership.joinedAt}")
        } else {
            logger.warn("사용자 멤버십 없음: chatRoomId=$chatRoomId, userId=$userId")
        }

        // 멤버 확인
        val isMember = chatRoomMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)
        logger.info("멤버 확인 결과: chatRoomId=$chatRoomId, userId=$userId, isMember=$isMember")

        if (!isMember) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다.")
        }

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val messagePage = messageRepository.findByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(chatRoomId, pageable)

        val messageInfos = messagePage.content.map { message ->
            convertToMessageInfo(message, userId)
        }.reversed() // 시간순으로 정렬

        logger.info("메시지 조회 완료: 메시지 수=${messageInfos.size}")

        return MessageListResponse(
            success = true,
            message = "메시지 목록 조회 성공",
            messages = messageInfos,
            hasNext = messagePage.hasNext(),
            totalElements = messagePage.totalElements
        )
    }

    // 채팅방 상세 조회
    @Transactional(readOnly = true)
    fun getChatRoomDetail(chatRoomId: Long, userId: Long): ChatRoomInfo {
        logger.info("채팅방 상세 조회: chatRoomId=$chatRoomId, userId=$userId")

        val chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow {
            IllegalArgumentException("존재하지 않는 채팅방입니다.")
        }

        // 멤버 확인 전에 모든 멤버 로그
        val allMembers = chatRoomMemberRepository.findByChatRoomId(chatRoomId)
        logger.info("채팅방 멤버 목록: ${allMembers.map { "userId=${it.userId}" }.joinToString(", ")}")

        // 멤버 확인
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId)) {
            logger.error("채팅방 멤버가 아님: chatRoomId=$chatRoomId, userId=$userId")
            throw IllegalArgumentException("채팅방 멤버가 아닙니다.")
        }

        val members = chatRoomMemberRepository.findByChatRoomId(chatRoomId)
        val memberInfos = members.mapNotNull { member ->
            userRepository.findById(member.userId).orElse(null)?.let { user ->
                ChatMemberInfo(
                    userId = user.userId,
                    userName = user.name,
                    profileImage = user.profileImage,
                    joinedAt = member.joinedAt,
                    lastReadAt = member.lastReadAt
                )
            }
        }

        val memberCount = members.size.toLong()
        val lastMessage = messageRepository.findTopByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(chatRoomId)
        val unreadCount = messageRepository.countUnreadMessages(chatRoomId, userId)

        // 채팅방 이름 설정
        val displayName = if (chatRoom.type == ChatRoomType.personal && chatRoom.name == null) {
            getPersonalChatRoomName(chatRoomId, userId)
        } else {
            chatRoom.name ?: "채팅방"
        }

        return ChatRoomInfo(
            chatRoomId = chatRoom.chatRoomId,
            name = displayName,
            type = chatRoom.type,
            memberCount = memberCount,
            lastMessage = lastMessage?.let { convertToMessageInfo(it, userId) },
            unreadCount = unreadCount,
            createdAt = chatRoom.createdAt,
            members = memberInfos,
            currentUserId = userId
        )
    }

    // 채팅방 목록 조회 - 디버깅 로그 추가
    @Transactional(readOnly = true)
    fun getChatRoomList(userId: Long): ChatRoomListResponse {
        logger.info("채팅방 목록 조회 시작: userId=$userId")

        // 사용자가 속한 모든 채팅방 ID 조회
        val chatRoomIds = chatRoomMemberRepository.findChatRoomIdsByUserId(userId)
        logger.info("사용자가 속한 채팅방 ID 목록: $chatRoomIds")

        val chatRooms = chatRoomRepository.findByUserId(userId)
        logger.info("조회된 채팅방 수: ${chatRooms.size}")

        val chatRoomInfos = chatRooms.map { chatRoom ->
            val members = chatRoomMemberRepository.findByChatRoomId(chatRoom.chatRoomId)
            val memberCount = members.size.toLong()

            logger.info("채팅방 ${chatRoom.chatRoomId}의 멤버 수: $memberCount")

            // 멤버 정보 생성 (개인 채팅의 경우 상대방 정보만 포함)
            val memberInfos = if (chatRoom.type == ChatRoomType.personal) {
                members.filter { it.userId != userId }.mapNotNull { member ->
                    userRepository.findById(member.userId).orElse(null)?.let { user ->
                        ChatMemberInfo(
                            userId = user.userId,
                            userName = user.name,
                            profileImage = user.profileImage,
                            joinedAt = member.joinedAt,
                            lastReadAt = member.lastReadAt
                        )
                    }
                }
            } else {
                members.mapNotNull { member ->
                    userRepository.findById(member.userId).orElse(null)?.let { user ->
                        ChatMemberInfo(
                            userId = user.userId,
                            userName = user.name,
                            profileImage = user.profileImage,
                            joinedAt = member.joinedAt,
                            lastReadAt = member.lastReadAt
                        )
                    }
                }
            }

            val lastMessage =
                messageRepository.findTopByChatRoomIdAndIsDeletedFalseOrderByCreatedAtDesc(chatRoom.chatRoomId)
            val unreadCount = messageRepository.countUnreadMessages(chatRoom.chatRoomId, userId)

            // 채팅방 이름 설정 (개인 채팅의 경우 상대방 이름)
            val displayName = if (chatRoom.type == ChatRoomType.personal && chatRoom.name == null) {
                memberInfos.firstOrNull()?.userName ?: "알 수 없음"
            } else {
                chatRoom.name ?: "채팅방"
            }

            ChatRoomInfo(
                chatRoomId = chatRoom.chatRoomId,
                name = displayName,
                type = chatRoom.type,
                memberCount = memberCount,
                lastMessage = lastMessage?.let { convertToMessageInfo(it, userId) },
                unreadCount = unreadCount,
                createdAt = chatRoom.createdAt,
                members = memberInfos,
                currentUserId = userId
            )
        }

        logger.info("채팅방 목록 조회 완료: 채팅방 수=${chatRoomInfos.size}")

        return ChatRoomListResponse(
            success = true,
            message = "채팅방 목록 조회 성공",
            chatRooms = chatRoomInfos
        )
    }

    // Helper 메서드들은 동일...
    private fun convertToMessageInfo(message: Message, currentUserId: Long): MessageInfo {
        val sender = userRepository.findById(message.senderId).orElse(null)
        val isRead = messageReadStatusRepository.existsByMessageIdAndUserId(message.messageId, currentUserId)
        val readCount = messageReadStatusRepository.countByMessageId(message.messageId)

        return MessageInfo(
            messageId = message.messageId,
            chatRoomId = message.chatRoomId,
            senderId = message.senderId,
            senderName = sender?.name ?: "알 수 없음",
            senderProfileImage = sender?.profileImage,
            content = message.content,
            messageType = message.messageType,
            fileUrl = message.fileUrl,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            updatedAt = message.updatedAt,
            readCount = readCount,
            isRead = isRead || message.senderId == currentUserId
        )
    }

    private fun getPersonalChatRoomName(chatRoomId: Long, userId: Long): String {
        val members = chatRoomMemberRepository.findByChatRoomId(chatRoomId)
        val otherMember = members.find { it.userId != userId }

        return otherMember?.let { member ->
            userRepository.findById(member.userId).orElse(null)?.name
        } ?: "Unknown"
    }

    // 메시지 전송
    fun sendMessage(request: SendMessageRequest, senderId: Long): SendMessageResponse {
        logger.info("메시지 전송: chatRoomId=${request.chatRoomId}, senderId=$senderId")

        // 채팅방 존재 확인
        val chatRoom = chatRoomRepository.findById(request.chatRoomId).orElseThrow {
            IllegalArgumentException("존재하지 않는 채팅방입니다.")
        }

        // 멤버 확인
        if (!chatRoomMemberRepository.existsByChatRoomIdAndUserId(request.chatRoomId, senderId)) {
            throw IllegalArgumentException("채팅방 멤버가 아닙니다.")
        }

        // 메시지 저장
        val message = Message(
            chatRoomId = request.chatRoomId,
            senderId = senderId,
            content = request.content,
            messageType = request.messageType,
            fileUrl = request.fileUrl
        )
        val savedMessage = messageRepository.save(message)

        // 채팅방 마지막 메시지 시간 업데이트
        chatRoom.lastMessageAt = savedMessage.createdAt
        chatRoomRepository.save(chatRoom)

        // WebSocket으로 실시간 전송
        val sender = userRepository.findById(senderId).orElse(null)
        val webSocketMessage = WebSocketMessage(
            type = "MESSAGE",
            chatRoomId = request.chatRoomId,
            senderId = senderId,
            senderName = sender?.name ?: "Unknown",
            content = request.content,
            messageId = savedMessage.messageId
        )

        messagingTemplate.convertAndSend("/topic/chat/${request.chatRoomId}", webSocketMessage)

        logger.info("메시지 전송 완료: messageId=${savedMessage.messageId}")

        return SendMessageResponse(
            success = true,
            message = "메시지가 전송되었습니다.",
            messageId = savedMessage.messageId,
            createdAt = savedMessage.createdAt
        )
    }

    // 메시지 읽음 처리
    fun markMessagesAsRead(chatRoomId: Long, userId: Long) {
        logger.info("메시지 읽음 처리: chatRoomId=$chatRoomId, userId=$userId")

        val member = chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
            ?: throw IllegalArgumentException("채팅방 멤버가 아닙니다.")

        // 마지막 읽은 시간 업데이트
        member.lastReadAt = LocalDateTime.now()
        chatRoomMemberRepository.save(member)

        // WebSocket으로 읽음 알림 전송
        val webSocketMessage = WebSocketMessage(
            type = "READ",
            chatRoomId = chatRoomId,
            senderId = userId,
            senderName = ""
        )

        messagingTemplate.convertAndSend("/topic/chat/$chatRoomId", webSocketMessage)
    }

    // 메시지 삭제 (논리적 삭제)
    fun deleteMessage(messageId: Long, userId: Long) {
        val message = messageRepository.findById(messageId).orElseThrow {
            IllegalArgumentException("존재하지 않는 메시지입니다.")
        }

        // 권한 확인 (본인만 삭제 가능)
        if (message.senderId != userId) {
            throw IllegalArgumentException("메시지를 삭제할 권한이 없습니다.")
        }

        // 직접 필드 수정 방식으로 변경
        message.isDeleted = true
        message.content = "삭제된 메시지입니다."
        message.updatedAt = LocalDateTime.now()
        messageRepository.save(message)

        // WebSocket으로 삭제 알림 전송
        val webSocketMessage = WebSocketMessage(
            type = "DELETE",
            chatRoomId = message.chatRoomId,
            senderId = userId,
            senderName = "",
            messageId = messageId
        )

        messagingTemplate.convertAndSend("/topic/chat/${message.chatRoomId}", webSocketMessage)
    }

    // 채팅방 나가기
    fun leaveChatRoom(chatRoomId: Long, userId: Long) {
        logger.info("채팅방 나가기: chatRoomId=$chatRoomId, userId=$userId")

        val member = chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId)
            ?: throw IllegalArgumentException("채팅방 멤버가 아닙니다.")

        chatRoomMemberRepository.delete(member)

        // 시스템 메시지 전송
        val user = userRepository.findById(userId).orElse(null)
        val systemMessage = Message(
            chatRoomId = chatRoomId,
            senderId = userId,
            content = "${user?.name ?: "사용자"}님이 채팅방을 나갔습니다.",
            messageType = MessageType.system
        )
        messageRepository.save(systemMessage)

        logger.info("채팅방 나가기 완료")
    }

    // 채팅 이미지 업로드 (Multipart)
    fun uploadChatImage(file: MultipartFile, userId: Long): UploadImageResponse {
        logger.info("채팅 이미지 업로드: userId=$userId, fileName=${file.originalFilename}")

        // 파일 유효성 검사
        validateImageFile(file)

        return try {
            // 파일 확장자 추출
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast('.', "jpg")

            // 고유한 파일명 생성
            val fileName = "${UUID.randomUUID()}.$extension"

            // 업로드 디렉토리 생성
            val fullUploadPath = Paths.get(uploadDir, chatUploadPath)
            if (!Files.exists(fullUploadPath)) {
                Files.createDirectories(fullUploadPath)
            }

            // 파일 저장
            val filePath = fullUploadPath.resolve(fileName)
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            logger.info("채팅 이미지 저장 완료: $fileName")

            UploadImageResponse(
                success = true,
                message = "이미지가 업로드되었습니다.",
                imageUrl = "/uploads/chats/$fileName",
                fileName = fileName
            )

        } catch (e: IOException) {
            logger.error("채팅 이미지 저장 실패", e)
            throw RuntimeException("이미지 저장에 실패했습니다.")
        }
    }

    // 채팅 이미지 업로드 (Base64)
    fun uploadChatImageBase64(base64Image: String, userId: Long): UploadImageResponse {
        logger.info("채팅 Base64 이미지 업로드: userId=$userId")

        return try {
            // Base64 디코딩
            val imageBytes = Base64.getDecoder().decode(base64Image)

            // 파일 크기 검사 (5MB 제한)
            if (imageBytes.size > 5 * 1024 * 1024) {
                throw IllegalArgumentException("이미지 크기는 5MB를 초과할 수 없습니다.")
            }

            // 파일명 생성
            val fileName = "${UUID.randomUUID()}.jpg"

            // 업로드 디렉토리 생성
            val fullUploadPath = Paths.get(uploadDir, chatUploadPath)
            if (!Files.exists(fullUploadPath)) {
                Files.createDirectories(fullUploadPath)
            }

            // 파일 저장
            val filePath = fullUploadPath.resolve(fileName)
            Files.write(filePath, imageBytes)

            logger.info("채팅 Base64 이미지 저장 완료: $fileName")

            UploadImageResponse(
                success = true,
                message = "이미지가 업로드되었습니다.",
                imageUrl = "/uploads/chats/$fileName",
                fileName = fileName
            )

        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("채팅 Base64 이미지 저장 실패", e)
            throw RuntimeException("이미지 저장에 실패했습니다.")
        }
    }

    // 이미지 파일 유효성 검사
    private fun validateImageFile(file: MultipartFile) {
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

        // 허용된 확장자 검사
        val originalFilename = file.originalFilename ?: ""
        val extension = originalFilename.substringAfterLast('.').lowercase()
        val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "webp")

        if (extension !in allowedExtensions) {
            throw IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)")
        }
    }
}