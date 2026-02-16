package wisoft.nextframe.payment.application.payment.exception;

public class PaymentGatewayExternalCallFailedException extends RuntimeException {
	public PaymentGatewayExternalCallFailedException(String operation, Throwable cause) {
		super("결제 게이트웨이 외부 호출 실패. operation=" + operation, cause);
	}
}
