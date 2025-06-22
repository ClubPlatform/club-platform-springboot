package com.devse.club_platform_server.controller

import com.devse.club_platform_server.dto.request.*
import com.devse.club_platform_server.dto.response.*
import com.devse.club_platform_server.service.ChatService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {
    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    // 채팅방 생성
    @PostMapping("/rooms")
    fun createChatRoom(
        @Valid @RequestBody request: CreateChatRoomRequest,
        authentication: Authentication
    ): ResponseEntity<CreateChatRoomResponse> {
        val userId = authentication.principal as Long
        logger.info("채팅방 생성 요청: userId=$userId, type=${request.type}")

        return try {
            val response = chatService.createChatRoom(request, userId)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("채팅방 생성 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                CreateChatRoomResponse(
                    success = false,
                    message = e.message ?: "채팅방 생성에 실패했습니다."
                )
            )
        }
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    fun getChatRoomList(authentication: Authentication): ResponseEntity<ChatRoomListResponse> {
        val userId = authentication.principal as Long
        logger.info("채팅방 목록 조회: userId=$userId")

        return try {
            val response = chatService.getChatRoomList(userId)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("채팅방 목록 조회 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                ChatRoomListResponse(
                    success = false,
                    message = e.message ?: "채팅방 목록을 조회할 수 없습니다."
                )
            )
        }
    }

    // 채팅방 상세 조회
    @GetMapping("/rooms/{chatRoomId}")
    fun getChatRoomDetail(
        @PathVariable chatRoomId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChatRoomInfo>> {
        val userId = authentication.principal as Long
        logger.info("채팅방 상세 조회: chatRoomId=$chatRoomId, userId=$userId")

        return try {
            val chatRoomInfo = chatService.getChatRoomDetail(chatRoomId, userId)
            ResponseEntity.ok(ApiResponse.success("채팅방 조회 성공", chatRoomInfo))
        } catch (e: Exception) {
            logger.error("채팅방 상세 조회 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                ApiResponse.error(e.message ?: "채팅방을 조회할 수 없습니다.")
            )
        }
    }

    // 메시지 목록 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    fun getMessageList(
        @PathVariable chatRoomId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        authentication: Authentication
    ): ResponseEntity<MessageListResponse> {
        val userId = authentication.principal as Long
        logger.info("메시지 목록 조회: chatRoomId=$chatRoomId, userId=$userId, page=$page")

        return try {
            val response = chatService.getMessageList(chatRoomId, userId, page, size)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("메시지 목록 조회 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                MessageListResponse(
                    success = false,
                    message = e.message ?: "메시지 목록을 조회할 수 없습니다."
                )
            )
        }
    }

    // 메시지 전송 (REST API)
    @PostMapping("/messages")
    fun sendMessage(
        @Valid @RequestBody request: SendMessageRequest,
        authentication: Authentication
    ): ResponseEntity<SendMessageResponse> {
        val userId = authentication.principal as Long
        logger.info("메시지 전송: chatRoomId=${request.chatRoomId}, userId=$userId")

        return try {
            val response = chatService.sendMessage(request, userId)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("메시지 전송 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                SendMessageResponse(
                    success = false,
                    message = e.message ?: "메시지 전송에 실패했습니다."
                )
            )
        }
    }

    // 메시지 읽음 처리
    @PostMapping("/rooms/{chatRoomId}/read")
    fun markMessagesAsRead(
        @PathVariable chatRoomId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("메시지 읽음 처리: chatRoomId=$chatRoomId, userId=$userId")

        return try {
            chatService.markMessagesAsRead(chatRoomId, userId)
            ResponseEntity.ok(ApiResponse.success("메시지를 읽음 처리했습니다.", ""))
        } catch (e: Exception) {
            logger.error("메시지 읽음 처리 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                ApiResponse.error(e.message ?: "메시지 읽음 처리에 실패했습니다.")
            )
        }
    }

    // 메시지 삭제
    @DeleteMapping("/messages/{messageId}")
    fun deleteMessage(
        @PathVariable messageId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("메시지 삭제: messageId=$messageId, userId=$userId")

        return try {
            chatService.deleteMessage(messageId, userId)
            ResponseEntity.ok(ApiResponse.success("메시지가 삭제되었습니다.", ""))
        } catch (e: Exception) {
            logger.error("메시지 삭제 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                ApiResponse.error(e.message ?: "메시지를 삭제할 수 없습니다.")
            )
        }
    }

    // 채팅방 나가기
    @PostMapping("/rooms/{chatRoomId}/leave")
    fun leaveChatRoom(
        @PathVariable chatRoomId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        val userId = authentication.principal as Long
        logger.info("채팅방 나가기: chatRoomId=$chatRoomId, userId=$userId")

        return try {
            chatService.leaveChatRoom(chatRoomId, userId)
            ResponseEntity.ok(ApiResponse.success("채팅방에서 나갔습니다.", ""))
        } catch (e: Exception) {
            logger.error("채팅방 나가기 실패: ${e.message}")
            ResponseEntity.badRequest().body(
                ApiResponse.error(e.message ?: "채팅방 나가기에 실패했습니다.")
            )
        }
    }

    // 채팅 이미지 업로드 (Multipart)
    @PostMapping("/upload/image", consumes = ["multipart/form-data"])
    fun uploadChatImage(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<UploadImageResponse> {
        val userId = authentication.principal as Long
        logger.info("채팅 이미지 업로드 요청: userId=$userId, fileName=${file.originalFilename}")

        return try {
            val response = chatService.uploadChatImage(file, userId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("채팅 이미지 업로드 실패 - 잘못된 입력: ${e.message}")
            ResponseEntity.badRequest().body(
                UploadImageResponse(
                    success = false,
                    message = e.message ?: "이미지 업로드에 실패했습니다."
                )
            )
        } catch (e: Exception) {
            logger.error("채팅 이미지 업로드 실패", e)
            ResponseEntity.internalServerError().body(
                UploadImageResponse(
                    success = false,
                    message = "서버 오류가 발생했습니다."
                )
            )
        }
    }

    // 채팅 이미지 업로드 (Base64)
    @PostMapping("/upload/image-base64")
    fun uploadChatImageBase64(
        @RequestBody request: ImageMessageRequest,
        authentication: Authentication
    ): ResponseEntity<UploadImageResponse> {
        val userId = authentication.principal as Long
        logger.info("채팅 Base64 이미지 업로드 요청: userId=$userId")

        return try {
            val response = chatService.uploadChatImageBase64(request.base64Image, userId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("채팅 Base64 이미지 업로드 실패 - 잘못된 입력: ${e.message}")
            ResponseEntity.badRequest().body(
                UploadImageResponse(
                    success = false,
                    message = e.message ?: "이미지 업로드에 실패했습니다."
                )
            )
        } catch (e: Exception) {
            logger.error("채팅 Base64 이미지 업로드 실패", e)
            ResponseEntity.internalServerError().body(
                UploadImageResponse(
                    success = false,
                    message = "서버 오류가 발생했습니다."
                )
            )
        }
    }
}

// WebSocket 메시지 처리를 위한 별도 컨트롤러
@RestController
class ChatWebSocketController(
    private val chatService: ChatService
) {
    private val logger = LoggerFactory.getLogger(ChatWebSocketController::class.java)

    // WebSocket을 통한 메시지 전송
    @MessageMapping("/chat.send")
    fun handleMessage(
        @Payload request: SendMessageRequest,
        authentication: Authentication
    ) {
        val userId = authentication.principal as Long
        logger.info("WebSocket 메시지 수신: chatRoomId=${request.chatRoomId}, userId=$userId")

        try {
            chatService.sendMessage(request, userId)
        } catch (e: Exception) {
            logger.error("WebSocket 메시지 처리 실패: ${e.message}")
        }
    }

    // 타이핑 상태 전송
    @MessageMapping("/chat.typing")
    fun handleTyping(
        @Payload typingStatus: Map<String, Any>,
        authentication: Authentication
    ) {
        val userId = authentication.principal as Long
        logger.debug("타이핑 상태: userId=$userId, status=$typingStatus")

        // 타이핑 상태를 다른 사용자들에게 브로드캐스트
        // 실제 구현은 필요에 따라 추가
    }
}