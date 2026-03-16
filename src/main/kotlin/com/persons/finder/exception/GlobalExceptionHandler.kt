package com.persons.finder.exception

import com.persons.finder.dto.response.ErrorResponse
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult
            .allErrors
            .filterIsInstance<FieldError>()
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = request.requestURI,
            fieldErrors = fieldErrors
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            path = request.requestURI,
            fieldErrors = fieldErrors
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Malformed JSON request",
            path = request.requestURI
        )
    }

    @ExceptionHandler(InvalidInputException::class)
    fun handleInvalidInput(
        ex: InvalidInputException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = ex.message ?: "Invalid request",
            path = request.requestURI
        )
    }

    @ExceptionHandler(PersonNotFoundException::class)
    fun handlePersonNotFound(
        ex: PersonNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.NOT_FOUND,
            message = ex.message ?: "Person not found",
            path = request.requestURI
        )
    }

    @ExceptionHandler(RateLimitExceededException::class)
    fun handleRateLimitExceeded(
        ex: RateLimitExceededException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.TOO_MANY_REQUESTS,
            message = ex.message ?: "Too many requests",
            path = request.requestURI
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Unexpected server error",
            path = request.requestURI
        )
    }

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        path: String,
        fieldErrors: Map<String, String> = emptyMap()
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    path = path,
                    fieldErrors = fieldErrors
                )
            )
    }
}
