package com.devse.club_platform_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/*
Spring MVC 웹 설정 구성
- 정적 리소스 핸들러 설정 및 경로 매핑
- 업로드된 파일 (프로필 이미지, 동아리 로고) 접근 경로 제공
 */

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
    }
}