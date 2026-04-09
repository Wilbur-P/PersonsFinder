package com.persons.finder.exception

import com.fasterxml.jackson.databind.JsonMappingException
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
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val missingField = extractMissingFieldPath(ex)
        if (missingField != null) {
            return buildResponse(
                status = HttpStatus.BAD_REQUEST,
                message = "Validation failed",
                path = request.requestURI,
                fieldErrors = mapOf(
                    missingField to "${missingField.substringAfterLast('.')} is required"
                )
            )
        }

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

    private fun extractMissingFieldPath(ex: HttpMessageNotReadableException): String? {
        val mappingException = generateSequence(ex.cause) { it.cause }
            .filterIsInstance<JsonMappingException>()
            .firstOrNull()
            ?: return null

        val segments = mappingException.path
            .mapNotNull { it.fieldName }
            .toMutableList()

        if (segments.isEmpty()) {
            val message = mappingException.originalMessage
            val parameterMatch = Regex("parameter ([A-Za-z0-9_]+)").find(message)
            val propertyMatch = Regex("property '([^']+)'").find(message)
            val fallbackField = parameterMatch?.groupValues?.get(1) ?: propertyMatch?.groupValues?.get(1)
            if (fallbackField != null) {
                segments += fallbackField
            }
        }

        return segments
            .takeIf { it.isNotEmpty() }
            ?.joinToString(".")
    }
}
