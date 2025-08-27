package wisoft.nextframe.schedulereservationticketing.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiErrorResponse;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.PerformanceScheduleMismatchException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.ReservationException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatAlreadyLockedException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.SeatNotDefinedException;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.TotalPriceMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 400 Bad Request - 클라이언트의 잘못된 요청 값을 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		log.warn("handleMethodArgumentNotValid: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("BAD_REQUEST");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	// 400 Bad Request - 요청 파라미터의 타입이 일치하지 않는 경우
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		log.warn("handleTypeMismatch: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("BAD_REQUEST");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	// 400 Bad Request - 공연 좌석 예매 시, 클라이언트가 잘못된 요청을 하는 경우
	@ExceptionHandler({
		TotalPriceMismatchException.class,
		SeatNotDefinedException.class,
		PerformanceScheduleMismatchException.class
	})
	public ResponseEntity<ApiErrorResponse> handleReservation(ReservationException ex) {
		log.warn("handleReservation: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("BAD_REQUEST");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	// 404 Not Found - 요청한 리소스를 찾을 수 없음
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
		log.warn("handleEntityNotFound: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("NOT_FOUND");
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	// 409 Conflict - 좌석이 이미 잠겨있음
	@ExceptionHandler(SeatAlreadyLockedException.class)
	public ResponseEntity<ApiErrorResponse> handleSeatAlreadyLockedException(SeatAlreadyLockedException ex) {
		log.warn("handleSeatAlreadyLocked: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("SEAT_ALREADY_LOCKED");
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	// 500 Internal Server Error - 처리되지 않은 모든 서버 내부 예외를 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
		log.error("handleException: {}", ex.getMessage());
		ApiErrorResponse response = new ApiErrorResponse("INTERNAL_SERVER_ERROR");
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
