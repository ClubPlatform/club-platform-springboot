package com.devse.club_platform_server.dto.response
/*
범용 API 응답 클래스
- 모든 API 엔드포인트에서 일관된 응답 형태를 제공할 때 사용
 */
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

/*
단순 성공/실패 응답 클래스
- 별도 데이터 없이 처리 결과만 알려주는 API에서 사용
- 삭제, 업데이트 등 단순 액션의 결과 반환 시 활용
 */
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