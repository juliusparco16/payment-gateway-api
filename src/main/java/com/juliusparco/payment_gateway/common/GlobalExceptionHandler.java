package com.juliusparco.payment_gateway.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.juliusparco.payment_gateway.auth.InvalidCredentialsException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, fieldErrors);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<ApiError> handleNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiError> handleInvalidCredentials(
			InvalidCredentialsException exception,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, Map.of());
	}

	private ResponseEntity<ApiError> build(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ApiError error = new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors);
		return ResponseEntity.status(status).body(error);
	}
}
