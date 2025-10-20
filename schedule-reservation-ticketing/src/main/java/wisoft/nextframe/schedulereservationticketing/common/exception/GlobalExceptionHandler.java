package wisoft.nextframe.schedulereservationticketing.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// @Valid 또는 @Validated binding error가 발생할 경우
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.warn("handleMethodArgumentNotValidException", ex);
		final ApiResponse<?> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	// 주로 @RequestParam의 enum으로 binding하지 못했을 경우
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex
	) {
		log.warn("handleMethodArgumentTypeMismatchException", ex);
		final ApiResponse<?> response = ApiResponse.error(ErrorCode.INVALID_TYPE_VALUE);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	// 도메인 관련 예외를 처리
	@ExceptionHandler(DomainException.class)
	protected ResponseEntity<ApiResponse<?>> handleDomainException(DomainException ex) {
		log.warn("BusinessException: {}", ex.getMessage());
		final ErrorCode errorCode = ex.getErrorCode();
		final ApiResponse<?> response = ApiResponse.error(errorCode);
		return new ResponseEntity<>(response, errorCode.getHttpStatus());
	}

	// 처리하지 못한 나머지 모든 예외를 처리
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<?>> handleException(Exception ex, HttpServletRequest request) {
		log.error("Unhandled Exception. Request: {} {}", request.getMethod(), request.getRequestURI(), ex);
		final ApiResponse<?> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
