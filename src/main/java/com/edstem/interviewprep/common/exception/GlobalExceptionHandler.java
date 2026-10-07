package com.edstem.interviewprep.common.exception;

import com.edstem.interviewprep.common.dto.response.ErrorResponse;
import com.edstem.interviewprep.common.dto.response.FieldErrorDetail;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(
      ApiException exception, HttpServletRequest request) {
    ErrorResponse body =
        ErrorResponse.of(
            exception.getStatus().value(),
            exception.getCode(),
            exception.getMessage(),
            request.getRequestURI());
    return ResponseEntity.status(exception.getStatus()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldErrorDetail(error.getField(), messageOf(error)))
            .sorted(Comparator.comparing(FieldErrorDetail::field))
            .toList();
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Request validation failed",
            request.getRequestURI(),
            fieldErrors);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    List<FieldErrorDetail> fieldErrors =
        exception.getConstraintViolations().stream()
            .map(
                violation ->
                    new FieldErrorDetail(
                        lastNodeOf(violation.getPropertyPath().toString()), violation.getMessage()))
            .sorted(Comparator.comparing(FieldErrorDetail::field))
            .toList();
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Request validation failed",
            request.getRequestURI(),
            fieldErrors);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    if (exception.getCause() instanceof InvalidFormatException invalidFormat) {
      String field = fieldPathOf(invalidFormat);
      if (!field.isEmpty()) {
        ErrorResponse rejected =
            ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                List.of(new FieldErrorDetail(field, rejectedValueMessageOf(invalidFormat))));
        return ResponseEntity.badRequest().body(rejected);
      }
    }
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MALFORMED_REQUEST",
            "Request body is missing or not readable",
            request.getRequestURI());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_PARAMETER",
            "A request parameter has an unsupported value",
            request.getRequestURI(),
            List.of(
                new FieldErrorDetail(
                    exception.getName(), "has an unsupported value: " + exception.getValue())));
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException exception, HttpServletRequest request) {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MISSING_PARAMETER",
            "A required request parameter is missing",
            request.getRequestURI(),
            List.of(new FieldErrorDetail(exception.getParameterName(), "is required")));
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleUploadTooLarge(
      MaxUploadSizeExceededException exception, HttpServletRequest request) {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            "PAYLOAD_TOO_LARGE",
            "The uploaded content is larger than this service accepts",
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResource(
      NoResourceFoundException exception, HttpServletRequest request) {
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "RESOURCE_NOT_FOUND",
            "No endpoint exists at this path",
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    log.error("Unhandled exception for {}", request.getRequestURI(), exception);
    ErrorResponse body =
        ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_ERROR",
            "The request could not be completed",
            request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private String messageOf(FieldError error) {
    return error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
  }

  private String lastNodeOf(String propertyPath) {
    int lastSeparator = propertyPath.lastIndexOf('.');
    return lastSeparator == -1 ? propertyPath : propertyPath.substring(lastSeparator + 1);
  }

  private String fieldPathOf(InvalidFormatException exception) {
    return exception.getPath().stream()
        .map(JsonMappingException.Reference::getFieldName)
        .filter(Objects::nonNull)
        .collect(Collectors.joining("."));
  }

  private String rejectedValueMessageOf(InvalidFormatException exception) {
    Class<?> targetType = exception.getTargetType();
    if (targetType != null && targetType.isEnum()) {
      String accepted =
          Arrays.stream(targetType.getEnumConstants())
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      return "must be one of: " + accepted;
    }
    return "has an unsupported value: " + exception.getValue();
  }
}
