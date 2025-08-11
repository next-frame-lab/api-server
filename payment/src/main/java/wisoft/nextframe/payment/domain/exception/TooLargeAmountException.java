package wisoft.nextframe.payment.domain.exception;

public class TooLargeAmountException extends PaymentException {
	public TooLargeAmountException() {
		super("결제 금액은 최대 1천만 원 미만이어야 합니다.");
	}
}