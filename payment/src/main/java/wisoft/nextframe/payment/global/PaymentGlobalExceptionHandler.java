package wisoft.nextframe.payment.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.ReservationNotFoundException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentException;
import wisoft.nextframe.payment.domain.refund.exception.RefundException;

@Slf4j
@RestControllerAdvice
public class PaymentGlobalExceptionHandler {

	@ExceptionHandler(RefundException.class)
	public ResponseEntity<ErrorResponse> handleRefundException(RefundException ex) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("REFUND_ERROR", ex.getMessage()));
	}

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("PAYMENT_ERROR", ex.getMessage()));
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleReservationNotFoundException(ReservationNotFoundException ex) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("RESERVATION_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Unexpected error occurred: ", ex);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("INTERNAL_ERROR", "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
	}


}
