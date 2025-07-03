package wisoft.nextframe.payment.presentation.payment.dto;

public record PaymentConfirmRequest(
	String paymentKey,
	String orderId,
	Integer amount
) {
}
