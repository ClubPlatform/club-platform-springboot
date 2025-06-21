package com.devse.club_platform_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // 클라이언트가 구독할 수 있는 destination prefix
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(longArrayOf(10000, 10000)) // 하트비트 설정 (10초)

        // 클라이언트가 메시지를 보낼 때 사용할 destination prefix
        config.setApplicationDestinationPrefixes("/app")

        // 사용자별 메시지를 위한 prefix
        config.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns(
                "http://localhost:*",
                "http://10.0.2.2:*",  // Android 에뮬레이터
                "http://*.devse.community",
                "https://*.devse.community",
                "*" // 개발용 (프로덕션에서는 제거)
            )
            .setHandshakeHandler(DefaultHandshakeHandler())
            .withSockJS()
            .setHeartbeatTime(25000) // SockJS 하트비트 (25초)
            .setDisconnectDelay(5000) // 연결 해제 지연 (5초)
            .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry
            .setMessageSizeLimit(128 * 1024) // 메시지 크기 제한 (128KB)
            .setSendTimeLimit(20 * 1000) // 전송 타임아웃 (20초)
            .setSendBufferSizeLimit(512 * 1024) // 전송 버퍼 크기 (512KB)
            .setTimeToFirstMessage(30 * 1000) // 첫 메시지까지 대기 시간 (30초)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration
            .interceptors(WebSocketAuthInterceptor()) // 인증 인터셉터 추가
            .taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(8)
            .queueCapacity(100)
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration
            .taskExecutor()
            .corePoolSize(4)
            .maxPoolSize(8)
    }
}

// WebSocket 인증 인터셉터
@Configuration
class WebSocketAuthInterceptor : org.springframework.messaging.support.ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(WebSocketAuthInterceptor::class.java)

    override fun preSend(
        message: org.springframework.messaging.Message<*>,
        channel: org.springframework.messaging.MessageChannel
    ): org.springframework.messaging.Message<*>? {
        val accessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor.wrap(message)

        // CONNECT 프레임일 때 인증 처리
        if (org.springframework.messaging.simp.stomp.StompCommand.CONNECT == accessor.command) {
            val authToken = accessor.getNativeHeader("Authorization")?.firstOrNull()

            if (authToken != null && authToken.startsWith("Bearer ")) {
                // JWT 토큰 검증 로직 추가 필요
                logger.debug("WebSocket connection with auth token")
            } else {
                logger.warn("WebSocket connection without auth token")
            }
        }

        return message
    }
}