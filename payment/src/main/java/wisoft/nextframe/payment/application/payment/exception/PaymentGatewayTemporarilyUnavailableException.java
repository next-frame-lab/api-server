package wisoft.nextframe.payment.application.payment.exception;

public class PaymentGatewayTemporarilyUnavailableException extends RuntimeException {
	public PaymentGatewayTemporarilyUnavailableException(String operation, Throwable cause) {
		super("결제 게이트웨이가 일시적으로 불가능합니다. operation=" + operation, cause);
	}
}
