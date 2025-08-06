package wisoft.nextframe.payment.domain.payment.exception;

public class RefundAlreadyExistsException extends PaymentException {
	public RefundAlreadyExistsException() {
		super("이미 환불된 결제입니다.");
	}
}