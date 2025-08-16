package wisoft.nextframe.payment.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import wisoft.nextframe.payment.domain.payment.exception.PaymentException;
import wisoft.nextframe.payment.domain.refund.exception.RefundException;

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
}
