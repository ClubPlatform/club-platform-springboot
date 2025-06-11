package com.devse.club_platform_server.dto.response

// 공통 API 응답 클래스
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> error(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = data
            )
        }
    }
}

// 단순 성공/실패 응답
data class SimpleResponse(
    val success: Boolean,
    val message: String,
) {
    companion object {
        fun success(message: String): SimpleResponse {
            return SimpleResponse(success = true, message = message)
        }

        fun error(message: String): SimpleResponse {
            return SimpleResponse(success = false, message = message)
        }
    }
}