package com.kush.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.kush.exception.BadRequestException;
import com.kush.payload.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("No endpoint found for " + ex.getHttpMethod() + " " + path(ex.getResourcePath())));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("No endpoint found for " + ex.getHttpMethod() + " " + path(ex.getRequestURL())));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedHttpMethods() == null || ex.getSupportedHttpMethods().isEmpty()
                ? ""
                : ". Supported methods: " + ex.getSupportedHttpMethods();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure("Method " + ex.getMethod() + " is not supported for this endpoint" + supported));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "the expected type";
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Parameter '" + ex.getName() + "' must be of type " + requiredType));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("Missing required parameter '" + ex.getParameterName() + "'"));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler({ MaxUploadSizeExceededException.class, MultipartException.class })
    public ResponseEntity<ApiResponse<Void>> handleMultipart(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("CSV upload is invalid or too large"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "invalid value",
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return ResponseEntity.badRequest().body(ApiResponse.failure("Validation failed", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return ResponseEntity.badRequest().body(ApiResponse.failure("Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("Malformed request body"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            String detail = errorResponse.getBody().getDetail();
            String message = (detail == null || detail.isBlank()) ? "Request could not be processed" : detail;
            return ResponseEntity.status(errorResponse.getStatusCode()).body(ApiResponse.failure(message));
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("An unexpected error occurred"));
    }

    private static String path(String value) {
        if (value == null || value.isBlank()) {
            return "/";
        }
        return value.startsWith("/") ? value : "/" + value;
    }
}
