package wisoft.nextframe.payment.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.ReservationExpiredException;
import wisoft.nextframe.payment.application.payment.exception.ReservationNotFoundException;
import wisoft.nextframe.payment.application.refund.RefundCancelFailedException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedFailedException;
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

	@ExceptionHandler(PaymentConfirmedFailedException.class)
	public ResponseEntity<ErrorResponse> handlePaymentConfirmedFailedException(PaymentConfirmedFailedException ex) {
		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse("PAYMENT_CONFIRM_FAILED", mapTossErrorToMessage(ex.getErrorCode())));
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

	@ExceptionHandler(ReservationExpiredException.class)
	public ResponseEntity<ErrorResponse> handleReservationExpiredException(ReservationExpiredException ex) {
		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(new ErrorResponse("RESERVATION_EXPIRED", ex.getMessage()));
	}

	@ExceptionHandler(PaymentGatewayTemporarilyUnavailableException.class)
	public ResponseEntity<ErrorResponse> handlePaymentGatewayUnavailable(PaymentGatewayTemporarilyUnavailableException ex) {
		log.warn("결제 게이트웨이 일시 불가: {}", ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body(new ErrorResponse("PAYMENT_GATEWAY_UNAVAILABLE", "결제 서비스가 일시적으로 불가능합니다. 잠시 후 다시 시도해 주세요."));
	}

	@ExceptionHandler(RefundCancelFailedException.class)
	public ResponseEntity<ErrorResponse> handleRefundCancelFailedException(RefundCancelFailedException ex) {
		log.error("결제사 환불 처리 실패: {}", ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.BAD_GATEWAY)
			.body(new ErrorResponse("REFUND_CANCEL_FAILED", "결제사 환불 처리에 실패했습니다. 잠시 후 다시 시도해주세요."));
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

	private String mapTossErrorToMessage(String tossCode) {
		return switch (tossCode) {
			case "ALREADY_PROCESSED_PAYMENT" -> "이미 결제가 처리되었습니다.";
			case "INVALID_API_KEY", "UNAUTHORIZED_KEY" -> "시스템 오류가 발생했습니다. 서비스 관리자에게 문의하세요.";
			case "PROVIDER_ERROR" -> "결제사에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
			case "REJECT_CARD_PAYMENT" -> "카드 결제가 거절되었습니다. 카드 정보를 확인하세요.";
			case "FDS_ERROR" -> "거래가 제한되었습니다. 고객센터에 문의하세요.";
			default -> "결제에 실패했습니다. 잠시 후 다시 시도하거나 고객센터에 문의하세요.";
		};
	}

}
