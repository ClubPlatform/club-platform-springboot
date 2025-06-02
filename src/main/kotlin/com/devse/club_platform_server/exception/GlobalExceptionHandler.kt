package com.devse.club_platform_server.exception

import com.devse.club_platform_server.dto.RegisterResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<RegisterResponse> {
        val errorMessage = e.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "입력값이 올바르지 않습니다."

        logger.warn("유효성 검사 오류: $errorMessage")

        val response = RegisterResponse(
            success = false,
            message = errorMessage,
            userId = null
        )

        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<RegisterResponse> {
        logger.warn("잘못된 인수 오류: ${e.message}")

        val response = RegisterResponse(
            success = false,
            message = e.message ?: "잘못된 요청입니다.",
            userId = null
        )

        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<RegisterResponse> {
        logger.error("런타임 오류: ${e.message}", e)

        val response = RegisterResponse(
            success = false,
            message = e.message ?: "처리 중 오류가 발생했습니다.",
            userId = null
        )

        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<RegisterResponse> {
        logger.error("예상치 못한 오류", e)

        val response = RegisterResponse(
            success = false,
            message = "서버 오류가 발생했습니다.",
            userId = null
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}