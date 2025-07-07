package wisoft.nextframe.payment.exception;

public class RefundAlreadyExistsException extends PaymentException {
	public RefundAlreadyExistsException() {
		super("이미 환불된 결제입니다.");
	}
}