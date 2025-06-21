package com.devse.club_platform_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // 메시지를 구독하는 요청의 prefix
        config.enableSimpleBroker("/topic", "/queue")
        // 메시지를 발행하는 요청의 prefix
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws-chat")
            .setAllowedOrigins("*") // 실제 운영에서는 구체적인 도메인 지정 필요
            .withSockJS()
    }
}