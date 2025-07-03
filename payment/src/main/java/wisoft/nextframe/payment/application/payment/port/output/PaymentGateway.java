package wisoft.nextframe.payment.application.payment.port.output;

public interface PaymentGateway {
	PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount);

	record PaymentConfirmResult(
		boolean isSuccess,
		int totalAmount,
		String errorCode,
		String errorMessage
	) {
	}
}
