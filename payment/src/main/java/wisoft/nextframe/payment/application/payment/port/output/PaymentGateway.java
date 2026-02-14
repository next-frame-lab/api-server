package wisoft.nextframe.payment.application.payment.port.output;

public interface PaymentGateway {
	PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount);

	PaymentCancelResult cancelPayment(String orderId, int cancelAmount, String cancelReason);

	record PaymentConfirmResult(
		boolean isSuccess,
		int totalAmount,
		String errorCode,
		String errorMessage
	) {
	}

	record PaymentCancelResult(
		boolean isSuccess,
		int cancelAmount,
		String transactionKey,
		String errorCode,
		String errorMessage
	) {
	}
}
