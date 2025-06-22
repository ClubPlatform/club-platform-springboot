package com.devse.club_platform_server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@Configuration
class WebConfig : WebMvcConfigurer {

    // ChatService와 동일하게 절대경로 사용
    private val uploadDir = "/uploads"

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 프로필 이미지 정적 리소스 핸들러
        registry.addResourceHandler("/uploads/profiles/**")
            .addResourceLocations("file:${uploadDir}/profiles/")
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))

        // 동아리 로고 이미지 정적 리소스 핸들러
        registry.addResourceHandler("/uploads/clubs/**")
            .addResourceLocations("file:${uploadDir}/clubs/")
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))

        // 채팅 이미지 정적 리소스 핸들러
        registry.addResourceHandler("/uploads/chats/**")
            .addResourceLocations("file:${uploadDir}/chats/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))

        // 일반 업로드 파일 핸들러
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:${uploadDir}/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:*",
                "http://10.0.2.2:*",
                "http://*.devse.community",
                "https://*.devse.community",
                "*" // 개발용 (프로덕션에서는 제거 필요)
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .exposedHeaders("Authorization", "Content-Type")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 로깅 인터셉터 추가 (옵션)
        registry.addInterceptor(LoggingInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/**", "/uploads/**")
    }

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
    }
}

// 요청/응답 로깅을 위한 인터셉터
@Configuration
class LoggingInterceptor : org.springframework.web.servlet.HandlerInterceptor {

    private val logger = org.slf4j.LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun preHandle(
        request: jakarta.servlet.http.HttpServletRequest,
        response: jakarta.servlet.http.HttpServletResponse,
        handler: Any
    ): Boolean {
        logger.debug("Request: {} {} from {}",
            request.method,
            request.requestURI,
            request.remoteAddr
        )
        return true
    }

    override fun afterCompletion(
        request: jakarta.servlet.http.HttpServletRequest,
        response: jakarta.servlet.http.HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        logger.debug("Response: {} for {} {}",
            response.status,
            request.method,
            request.requestURI
        )
        if (ex != null) {
            logger.error("Request failed", ex)
        }
    }
}