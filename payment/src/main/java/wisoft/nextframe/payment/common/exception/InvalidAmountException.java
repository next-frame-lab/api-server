package wisoft.nextframe.payment.common.exception;

public class InvalidAmountException extends RuntimeException {
	public InvalidAmountException() {
		super("금액은 음수일 수 없습니다.");
	}
}
