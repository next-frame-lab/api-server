package wisoft.nextframe.payment.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.ReservationNotFoundException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentException;
import wisoft.nextframe.payment.domain.refund.exception.RefundException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;

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

	@ExceptionHandler(PaymentGatewayTemporarilyUnavailableException.class)
	public ResponseEntity<ErrorResponse> handlePaymentGatewayUnavailable(PaymentGatewayTemporarilyUnavailableException ex) {
		log.warn("결제 게이트웨이 일시 불가: {}", ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(new ErrorResponse("PAYMENT_GATEWAY_UNAVAILABLE", "결제 서비스가 일시적으로 불가능합니다. 잠시 후 다시 시도해 주세요."));
	}

	@ExceptionHandler(PaymentGatewayExternalCallFailedException.class)
	public ResponseEntity<ErrorResponse> handlePaymentGatewayCallFailed(PaymentGatewayExternalCallFailedException ex) {
		log.error("결제 게이트웨이 호출 실패: {}", ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.BAD_GATEWAY)
			.body(new ErrorResponse("PAYMENT_GATEWAY_FAILED", "결제 처리 중 외부 서비스 오류가 발생했습니다."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Unexpected error occurred: ", ex);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("INTERNAL_ERROR", "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
	}


}
