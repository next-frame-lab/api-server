package wisoft.nextframe.payment.presentation.payment.dto;

public record PaymentConfirmResponse(
	String code,
	PaymentApprovedData data,
	String message
) {
	public boolean isSuccess() {
		return "SUCCESS".equalsIgnoreCase(code);
	}
}