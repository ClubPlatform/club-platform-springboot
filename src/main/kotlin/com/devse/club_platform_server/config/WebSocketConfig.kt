package com.devse.club_platform_server.config

import com.devse.club_platform_server.util.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.slf4j.LoggerFactory

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtTokenProvider: JwtTokenProvider
) : WebSocketMessageBrokerConfigurer {

    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    @Bean
    fun webSocketTaskScheduler(): TaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.setPoolSize(1)
        taskScheduler.setThreadNamePrefix("websocket-heartbeat-")
        taskScheduler.initialize()
        return taskScheduler
    }

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // 클라이언트가 구독할 수 있는 destination prefix
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(longArrayOf(10000, 10000)) // 하트비트 설정 (10초)
            .setTaskScheduler(webSocketTaskScheduler())

        // 클라이언트가 메시지를 보낼 때 사용할 destination prefix
        config.setApplicationDestinationPrefixes("/app")

        // 사용자별 메시지를 위한 prefix
        config.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 연결 엔드포인트 - 모든 origin 허용
        registry.addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*") // 모든 origin 허용
            .setHandshakeHandler(DefaultHandshakeHandler())
            .withSockJS()
            .setHeartbeatTime(25000)
            .setDisconnectDelay(5000)
            .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry
            .setMessageSizeLimit(512 * 1024) // 메시지 크기 제한 증가 (512KB)
            .setSendTimeLimit(60 * 1000) // 전송 타임아웃 증가 (60초)
            .setSendBufferSizeLimit(1024 * 1024) // 전송 버퍼 크기 증가 (1MB)
            .setTimeToFirstMessage(120 * 1000) // 첫 메시지 대기 시간 증가 (120초)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration
            .interceptors(WebSocketAuthInterceptor(jwtTokenProvider))
            .taskExecutor()
            .corePoolSize(8) // 스레드 풀 크기 증가
            .maxPoolSize(16)
            .queueCapacity(200)
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration
            .taskExecutor()
            .corePoolSize(8)
            .maxPoolSize(16)
    }
}

// WebSocket 인증 인터셉터 - 느슨한 인증
class WebSocketAuthInterceptor(
    private val jwtTokenProvider: JwtTokenProvider
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(WebSocketAuthInterceptor::class.java)

    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        // CONNECT 프레임일 때만 인증 처리 (옵션)
        if (StompCommand.CONNECT == accessor.command) {
            val authToken = accessor.getNativeHeader("Authorization")?.firstOrNull()

            if (authToken != null && authToken.startsWith("Bearer ")) {
                try {
                    val token = authToken.substring(7)

                    if (jwtTokenProvider.validateToken(token)) {
                        val userId = jwtTokenProvider.getUserIdFromToken(token)
                        val email = jwtTokenProvider.getEmailFromToken(token)

                        val authentication = UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            emptyList()
                        )

                        accessor.user = authentication
                        accessor.setHeader("userId", userId.toString())
                        accessor.setHeader("userEmail", email)

                        logger.info("WebSocket 인증 성공: userId=$userId")
                    } else {
                        logger.warn("유효하지 않은 JWT 토큰이지만 연결 허용")
                        // 토큰이 유효하지 않아도 연결 허용
                    }
                } catch (e: Exception) {
                    logger.error("WebSocket 인증 오류가 발생했지만 연결 허용", e)
                    // 오류가 발생해도 연결 허용
                }
            } else {
                logger.info("인증 토큰 없이 WebSocket 연결 - 익명 사용자로 처리")
                // 인증 없이도 연결 허용
            }
        }

        // 모든 메시지 허용 (SEND, SUBSCRIBE 등)
        return message
    }
}