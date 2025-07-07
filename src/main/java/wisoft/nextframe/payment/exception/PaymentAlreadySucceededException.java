package wisoft.nextframe.payment.exception;

public class PaymentAlreadySucceededException extends PaymentException {
	public PaymentAlreadySucceededException() {
		super("이미 결제 성공 처리된 건입니다.");
	}
}