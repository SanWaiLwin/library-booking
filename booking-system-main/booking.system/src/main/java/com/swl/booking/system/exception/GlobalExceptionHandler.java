package com.swl.booking.system.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.validation.ConstraintViolationException;
import com.swl.booking.system.response.ApiResponse;
import com.swl.booking.system.util.CommonConstant;
import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getBindingResult().getFieldErrors().forEach(error -> errorMessage.append(error.getField()).append(" ")
				.append(error.getDefaultMessage()).append("; "));
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleBindException(BindException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getBindingResult().getFieldErrors().forEach(error -> errorMessage.append(error.getField()).append(" ")
				.append(error.getDefaultMessage()).append("; "));
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getConstraintViolations().forEach(violation -> errorMessage.append(violation.getPropertyPath()).append(" ")
				.append(violation.getMessage()).append("; "));
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(AlreadyExitException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleAlreadyExitException(AlreadyExitException ex) {
		logger.warn("AlreadyExitException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
	}

	@ExceptionHandler(ResponseInfoException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleResponseInfoException(ResponseInfoException ex) {
		logger.warn("ResponseInfoException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
	}

	@ExceptionHandler(RdpException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Void> handleRdpException(RdpException ex) {
		logger.error("RdpException: {}", ex.getMessage(), ex);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Internal server error: " + ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
		logger.warn("IllegalArgumentException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Invalid argument: " + ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiResponse<Void> handleIllegalStateException(IllegalStateException ex) {
		logger.warn("IllegalStateException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Invalid state: " + ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Void> handleRuntimeException(RuntimeException ex) {
		logger.error("RuntimeException: {}", ex.getMessage(), ex);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		logger.warn("DataIntegrityViolationException: {}", ex.getMessage());
		String message = "Data integrity violation";
		if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
			message = "Duplicate entry - record already exists";
		}
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, message);
	}

	@ExceptionHandler(SQLException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Void> handleSQLException(SQLException ex) {
		logger.error("SQLException: {}", ex.getMessage(), ex);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Database error occurred");
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex) {
		logger.warn("AccessDeniedException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Access denied: " + ex.getMessage());
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiResponse<Void> handleAuthenticationException(AuthenticationException ex) {
		logger.warn("AuthenticationException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Authentication failed: " + ex.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException ex) {
		logger.warn("BadCredentialsException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Invalid credentials");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	public ApiResponse<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		logger.warn("HttpRequestMethodNotSupportedException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Method not allowed: " + ex.getMethod());
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException ex) {
		logger.warn("NoHandlerFoundException: {}", ex.getMessage());
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Endpoint not found: " + ex.getRequestURL());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ApiResponse<Void> handleGenericException(Exception ex, WebRequest request) {
		logger.error("Unhandled exception: {} at {}", ex.getMessage(), request.getDescription(false), ex);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "An unexpected error occurred");
	}
}
